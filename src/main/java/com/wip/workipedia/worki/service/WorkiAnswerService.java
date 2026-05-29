package com.wip.workipedia.worki.service;

import com.wip.workipedia.worki.domain.WorkiAnswer;
import com.wip.workipedia.worki.domain.WorkiQuestion;
import com.wip.workipedia.worki.dto.AnswerCreateRequest;
import com.wip.workipedia.worki.dto.AnswerResponse;
import com.wip.workipedia.worki.exception.WorkiAccessDeniedException;
import com.wip.workipedia.worki.exception.WorkiNotFoundException;
import com.wip.workipedia.worki.exception.WorkiPolicyViolationException;
import com.wip.workipedia.worki.repository.WorkiAnswerRepository;
import com.wip.workipedia.worki.repository.WorkiQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkiAnswerService {

    private final WorkiAnswerRepository answerRepository;
    private final WorkiQuestionRepository questionRepository;

    public AnswerResponse createAnswer(Long actorUserId, Long questionId, AnswerCreateRequest request) {
        WorkiQuestion question = getQuestionOrThrow(questionId);
        if (question.isAnswered()) {
            throw new WorkiPolicyViolationException("채택된 답변이 있어 더 이상 답변을 등록할 수 없습니다.");
        }
        WorkiAnswer answer = answerRepository.save(
                WorkiAnswer.create(questionId, actorUserId, request.content()));
        question.markInProgress();
        return AnswerResponse.from(answer);
    }

    public AnswerResponse acceptAnswer(Long actorUserId, Long answerId) {
        WorkiAnswer answer = answerRepository.findByAnswerIdAndDeletedAtIsNull(answerId)
                .orElseThrow(() -> new WorkiNotFoundException("답변을 찾을 수 없습니다. id=" + answerId));
        WorkiQuestion question = getQuestionOrThrow(answer.getQuestionId());
        if (!question.isAuthor(actorUserId)) {
            throw new WorkiAccessDeniedException("질문 작성자만 답변을 채택할 수 있습니다.");
        }
        if (question.isAnswered()) {
            throw new WorkiPolicyViolationException("이미 채택된 답변이 있습니다.");
        }
        answer.accept();
        question.markAnswered();
        return AnswerResponse.from(answer);
    }

    private WorkiQuestion getQuestionOrThrow(Long questionId) {
        return questionRepository.findByQuestionIdAndDeletedAtIsNull(questionId)
                .orElseThrow(() -> new WorkiNotFoundException("질문을 찾을 수 없습니다. id=" + questionId));
    }
}
