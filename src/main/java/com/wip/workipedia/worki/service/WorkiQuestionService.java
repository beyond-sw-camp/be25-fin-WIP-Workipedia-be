package com.wip.workipedia.worki.service;

import com.wip.workipedia.worki.domain.WorkiAnswer;
import com.wip.workipedia.worki.domain.WorkiQuestion;
import com.wip.workipedia.worki.dto.QuestionCreateRequest;
import com.wip.workipedia.worki.dto.QuestionDetailResponse;
import com.wip.workipedia.worki.dto.QuestionResponse;
import com.wip.workipedia.worki.dto.QuestionSummaryResponse;
import com.wip.workipedia.worki.dto.QuestionUpdateRequest;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.worki.repository.WorkiAnswerRepository;
import com.wip.workipedia.worki.repository.WorkiQuestionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public QuestionResponse create(Long actorUserId, QuestionCreateRequest request) {
        WorkiQuestion question = WorkiQuestion.create(
                actorUserId, request.title(), request.content(), request.sourceChatbotMessageId());
        return QuestionResponse.from(questionRepository.save(question));
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
        return QuestionDetailResponse.of(question, answers);
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
        return QuestionResponse.from(question);
    }

    private WorkiQuestion getQuestionOrThrow(Long questionId) {
        return questionRepository.findByQuestionIdAndDeletedAtIsNull(questionId)
                .orElseThrow(() -> new CustomException(ErrorType.WORKI_NOT_FOUND, "질문을 찾을 수 없습니다. id=" + questionId));
    }
}
