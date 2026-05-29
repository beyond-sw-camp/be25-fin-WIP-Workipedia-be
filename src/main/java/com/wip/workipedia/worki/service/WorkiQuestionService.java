package com.wip.workipedia.worki.service;

import com.wip.workipedia.worki.domain.WorkiAnswer;
import com.wip.workipedia.worki.domain.WorkiQuestion;
import com.wip.workipedia.worki.dto.QuestionCreateRequest;
import com.wip.workipedia.worki.dto.QuestionDetailResponse;
import com.wip.workipedia.worki.dto.QuestionResponse;
import com.wip.workipedia.worki.dto.QuestionSummaryResponse;
import com.wip.workipedia.worki.dto.QuestionUpdateRequest;
import com.wip.workipedia.worki.exception.WorkiAccessDeniedException;
import com.wip.workipedia.worki.exception.WorkiNotFoundException;
import com.wip.workipedia.worki.exception.WorkiPolicyViolationException;
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

    @Transactional
    public QuestionDetailResponse getDetail(Long questionId) {
        WorkiQuestion question = getQuestionOrThrow(questionId);
        question.increaseViewCount();
        List<WorkiAnswer> answers =
                answerRepository.findByQuestionIdAndDeletedAtIsNullOrderByCreatedAtAsc(questionId);
        return QuestionDetailResponse.of(question, answers);
    }

    @Transactional
    public QuestionResponse update(Long actorUserId, Long questionId, QuestionUpdateRequest request) {
        WorkiQuestion question = getQuestionOrThrow(questionId);
        if (!question.isAuthor(actorUserId)) {
            throw new WorkiAccessDeniedException("질문 작성자만 수정할 수 있습니다.");
        }
        if (!question.isWaiting()) {
            throw new WorkiPolicyViolationException("답변 대기(WAITING) 상태에서만 질문을 수정할 수 있습니다.");
        }
        question.updateContent(request.title(), request.content());
        return QuestionResponse.from(question);
    }

    private WorkiQuestion getQuestionOrThrow(Long questionId) {
        return questionRepository.findByQuestionIdAndDeletedAtIsNull(questionId)
                .orElseThrow(() -> new WorkiNotFoundException("질문을 찾을 수 없습니다. id=" + questionId));
    }
}
