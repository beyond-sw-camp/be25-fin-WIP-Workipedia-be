package com.wip.workipedia.worki.service;

import com.wip.workipedia.worki.domain.WorkiAnswer;
import com.wip.workipedia.worki.domain.WorkiQuestion;
import com.wip.workipedia.worki.dto.AnswerCreateRequest;
import com.wip.workipedia.worki.dto.AnswerResponse;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.notification.service.NotificationService;
import com.wip.workipedia.point.domain.PointReasonType;
import com.wip.workipedia.point.service.PointService;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.repository.UserRepository;
import com.wip.workipedia.worki.repository.WorkiAnswerRepository;
import com.wip.workipedia.worki.repository.WorkiQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkiAnswerService {

    private static final int ANSWER_POINT = 5;
    private static final int ANSWER_ACCEPTED_POINT = 5;
    private static final String ANSWER_RELATED_TYPE = "WORKI_ANSWER";

    private final WorkiAnswerRepository answerRepository;
    private final WorkiQuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final PointService pointService;

    public AnswerResponse createAnswer(Long actorUserId, Long questionId, AnswerCreateRequest request) {
        WorkiQuestion question = getQuestionOrThrow(questionId);
        if (question.isAnswered()) {
            throw new CustomException(ErrorType.WORKI_POLICY_VIOLATION, "채택된 답변이 있어 더 이상 답변을 등록할 수 없습니다.");
        }
        WorkiAnswer answer = answerRepository.save(
                WorkiAnswer.create(questionId, actorUserId, request.content()));
        question.markInProgress();
        // 본인이 작성한 질문에 직접 답변한 경우에는 별도 알림을 만들지 않는다.
        if (!question.isAuthor(actorUserId)) {
            pointService.earnPoint(actorUserId, ANSWER_POINT, PointReasonType.WORKI_ANSWER_CREATED, ANSWER_RELATED_TYPE, answer.getAnswerId());
            notificationService.createWorkiQuestionAnswered(
                    question.getAuthorId(), question.getQuestionId(), question.getTitle());
        }
        User author = userRepository.findById(actorUserId).orElse(null);
        return AnswerResponse.of(answer, author);
    }

    public AnswerResponse acceptAnswer(Long actorUserId, Long answerId) {
        WorkiAnswer answer = answerRepository.findByAnswerIdAndDeletedAtIsNull(answerId)
                .orElseThrow(() -> new CustomException(ErrorType.WORKI_NOT_FOUND, "답변을 찾을 수 없습니다. id=" + answerId));
        WorkiQuestion question = getQuestionOrThrow(answer.getQuestionId());

        if (!question.isAuthor(actorUserId)) {
            throw new CustomException(ErrorType.WORKI_FORBIDDEN, "질문 작성자만 답변을 채택할 수 있습니다.");
        }

        if (question.isAnswered()) {
            throw new CustomException(ErrorType.WORKI_POLICY_VIOLATION, "이미 채택된 답변이 있습니다.");
        }

        answer.accept();
        question.acceptAnswer(answer.getAnswerId());
        // 답변 채택 알림은 채택된 답변 작성자에게만 보낸다.
        if (!answer.getAuthorId().equals(actorUserId)) {
            pointService.earnPoint(answer.getAuthorId(), ANSWER_ACCEPTED_POINT, PointReasonType.WORKI_ANSWER_ACCEPTED, ANSWER_RELATED_TYPE, answer.getAnswerId());
            notificationService.createWorkiAnswerAccepted(
                    answer.getAuthorId(), answer.getAnswerId(), question.getQuestionId(), question.getTitle());
        }
        User author = userRepository.findById(answer.getAuthorId()).orElse(null);
        return AnswerResponse.of(answer, author);
    }

    private WorkiQuestion getQuestionOrThrow(Long questionId) {
        return questionRepository.findByQuestionIdAndDeletedAtIsNull(questionId)
                .orElseThrow(() -> new CustomException(ErrorType.WORKI_NOT_FOUND, "질문을 찾을 수 없습니다. id=" + questionId));
    }
}
