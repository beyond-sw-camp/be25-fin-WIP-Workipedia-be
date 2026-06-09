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
        return questionRepository.findByDeletedAtIsNull(pageable)
                .map(QuestionSummaryResponse::from);
    }

    // ToDo: 여기부분 부하 줄수 있음. 새로고침 계속 하면 update 해야하니 이부분 수정 방법 찾아야함.
   @Transactional // 왜 위에 readOnly = true 가 있는데 여기에 추가한 이유: 사실 더티 체킹을 어떻게 하냐에 따른 방법인데, 트랜젝션이 있으면, 동시성에 문제가 생겨버림. 2명이 동시에 조회수 10짜리 읽으면 12가 되어야 하는데 11이 됨.
    public QuestionDetailResponse getDetail(Long questionId) {
        // WorkiQuestion question = getQuestionOrThrow(questionId);
        // question.increaseViewCount(); // 여기 서비스에서 바꾸면 캡슐화 깨짐. 따로 메서드 빼서 바꾸는게 좋음.

        // 그냥 JPA 쿼리 사용해서 단순 뷰 업데이트 올림. 
        questionRepository.increaseViewCount(questionId);
        WorkiQuestion question = getQuestionOrThrow(questionId);
        
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
        return QuestionDetailResponse.of(
                question, authorsById.get(question.getAuthorId()), answerResponses);
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

        savedQuestions.forEach(saved ->
                eventPublisher.publishEvent(new WorkiQuestionChangedEvent(saved.getQuestionId()))
        );

        return savedQuestions.stream()
                .map(QuestionResponse::from)
                .toList();
    }
}
