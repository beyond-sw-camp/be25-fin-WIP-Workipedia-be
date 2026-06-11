package com.wip.workipedia.worki.service;

import com.wip.workipedia.worki.domain.WorkiAnswer;
import com.wip.workipedia.worki.domain.WorkiQuestion;
import com.wip.workipedia.worki.dto.AnswerResponse;
import com.wip.workipedia.worki.dto.QuestionCreateRequest;
import com.wip.workipedia.worki.dto.QuestionDetailResponse;
import com.wip.workipedia.worki.dto.QuestionResponse;
import com.wip.workipedia.worki.dto.QuestionSummaryResponse;
import com.wip.workipedia.worki.dto.QuestionUpdateRequest;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.reaction.domain.ReactionTargetType;
import com.wip.workipedia.reaction.domain.ReactionType;
import com.wip.workipedia.reaction.repository.ReactionRepository;
import com.wip.workipedia.reaction.repository.TargetLikeCount;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.repository.UserRepository;
import com.wip.workipedia.worki.event.WorkiQuestionChangedEvent;
import com.wip.workipedia.worki.repository.WorkiAnswerRepository;
import com.wip.workipedia.worki.repository.WorkiQuestionRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkiQuestionService {

    private final WorkiQuestionRepository questionRepository;
    private final WorkiAnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final ReactionRepository reactionRepository;
    private final WorkiViewCountService viewCountService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public QuestionResponse create(Long actorUserId, QuestionCreateRequest request) {
        WorkiQuestion question = WorkiQuestion.create(
                actorUserId, request.title(), request.content(), request.sourceChatbotMessageId());
        WorkiQuestion saved = questionRepository.save(question);
        // 커밋 후 검색 색인이 반영되도록 이벤트만 발행한다(실제 색인은 search가 구독 즉, search 서비스에서 처리).
        // 롤백 되면 색인 요청 삭제. 안하게 됨. 커밋과 색인 요청이 동시에 일어남.
        eventPublisher.publishEvent(new WorkiQuestionChangedEvent(saved.getQuestionId()));
        return QuestionResponse.from(saved);
    }

    public Page<QuestionSummaryResponse> list(Pageable pageable) {
        Page<WorkiQuestion> page = questionRepository.findByDeletedAtIsNull(pageable);

        // 페이지에 담긴 질문들의 좋아요 수를 한 번에 집계한다(질문마다 COUNT 하면 N+1).
        Map<Long, Long> likeCounts = loadQuestionLikeCounts(
                page.getContent().stream().map(WorkiQuestion::getQuestionId).toList());

        return page.map(question ->
                QuestionSummaryResponse.of(question, likeCounts.getOrDefault(question.getQuestionId(), 0L)));
    }

    private Map<Long, Long> loadQuestionLikeCounts(List<Long> questionIds) {
        if (questionIds.isEmpty()) {
            return Map.of();
        }
        return reactionRepository.countLikesByTargetIds(
                        ReactionTargetType.WORKI_QUESTION, ReactionType.LIKE, questionIds)
                .stream()
                .collect(Collectors.toMap(TargetLikeCount::getTargetId, TargetLikeCount::getLikeCount));
    }

    // 조회수는 더 이상 여기서 바로 DB에 UPDATE하지 않는다. Redis로 중복 조회를 막고 신규 조회만 누적한 뒤
    // 스케줄러가 일괄 반영한다(WorkiViewCountService 참고). readOnly 트랜잭션을 유지할 수 있게 됐다.
    public QuestionDetailResponse getDetail(Long questionId, Long viewerUserId) {
        WorkiQuestion question = getQuestionOrThrow(questionId);

        // 10분 내 같은 사용자의 재조회는 무시하고, 신규 조회만 Redis 임시 카운터에 누적한다.
        viewCountService.countView(questionId, viewerUserId);

        List<WorkiAnswer> answers =
                answerRepository.findByQuestionIdAndDeletedAtIsNullOrderByCreatedAtAsc(questionId);

        // 프론트 "부서 · 닉네임" 표시용. 질문 작성자 + 모든 답변 작성자를 한 번에 조회한다(답변마다 개별 조회하면 N+1).
        // 작성자가 탈퇴 등으로 없으면 null로 내려보내고(상세 조회 자체는 막지 않음), 닉네임/부서명 변환은 DTO가 null-safe하게 처리한다.
        Set<Long> authorIds = new HashSet<>();
        authorIds.add(question.getAuthorId());
        answers.forEach(answer -> authorIds.add(answer.getAuthorId()));
        Map<Long, User> authorsById = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        List<AnswerResponse> answerResponses = answers.stream()
                .map(answer -> AnswerResponse.of(answer, authorsById.get(answer.getAuthorId())))
                .toList();

        // 좋아요 개수는 reactions에서 COUNT로 집계해 내려준다(별도 컬럼 없이 단일 소스 유지).
        long likeCount = reactionRepository.countByTargetTypeAndTargetIdAndReactionType(
                ReactionTargetType.WORKI_QUESTION, questionId, ReactionType.LIKE);

        // 화면에는 DB값 + 아직 반영되지 않은 Redis 누적분을 합쳐 보여줘, 일괄 반영 지연을 사용자가 체감하지 않게 한다.
        long viewCount = question.getViewCount() + viewCountService.getPendingCount(questionId);

        return QuestionDetailResponse.of(
                question, authorsById.get(question.getAuthorId()), viewCount, likeCount, answerResponses);
    }

    @Transactional //여기의 경우는 더티체킹이 필요함
    public QuestionResponse update(Long actorUserId, Long questionId, QuestionUpdateRequest request) {
        WorkiQuestion question = getQuestionOrThrow(questionId);
        if (!question.isAuthor(actorUserId)) {
            throw new CustomException(ErrorType.WORKI_FORBIDDEN, "질문 작성자만 수정할 수 있습니다.");
        }
        if (!question.isWaiting()) {
            throw new CustomException(ErrorType.WORKI_POLICY_VIOLATION, "답변 대기(WAITING) 상태에서만 질문을 수정할 수 있습니다.");
        }
        question.updateContent(request.title(), request.content());
        // 커밋 후 검색 색인이 반영되도록 이벤트만 발행한다(실제 색인은 search가 구독 즉, search 서비스에서 처리).
        eventPublisher.publishEvent(new WorkiQuestionChangedEvent(question.getQuestionId()));
        return QuestionResponse.from(question);
    }

    private WorkiQuestion getQuestionOrThrow(Long questionId) {
        return questionRepository.findByQuestionIdAndDeletedAtIsNull(questionId)
                .orElseThrow(() -> new CustomException(ErrorType.WORKI_NOT_FOUND, "질문을 찾을 수 없습니다. id=" + questionId));
    }

    @Transactional
    public List<QuestionResponse> createBulk(Long actorUserId, List<QuestionCreateRequest> requests) {
        List<WorkiQuestion> questions = requests.stream()
                .map(request -> WorkiQuestion.create(
                        actorUserId,
                        request.title(),
                        request.content(),
                        request.sourceChatbotMessageId()
                ))
                .toList();

        List<WorkiQuestion> savedQuestions = questionRepository.saveAll(questions);

        savedQuestions.forEach(saved -> {
            eventPublisher.publishEvent(new WorkiQuestionChangedEvent(saved.getQuestionId()));
        });

        return savedQuestions.stream()
                .map(QuestionResponse::from)
                .toList();
    }
}
