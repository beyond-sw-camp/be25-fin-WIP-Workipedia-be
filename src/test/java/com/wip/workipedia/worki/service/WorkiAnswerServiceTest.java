package com.wip.workipedia.worki.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.worki.domain.QuestionStatus;
import com.wip.workipedia.worki.domain.WorkiAnswer;
import com.wip.workipedia.worki.domain.WorkiQuestion;
import com.wip.workipedia.worki.dto.AnswerCreateRequest;
import com.wip.workipedia.worki.dto.AnswerResponse;
import com.wip.workipedia.worki.repository.WorkiAnswerRepository;
import com.wip.workipedia.worki.repository.WorkiQuestionRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkiAnswerServiceTest {

    @Mock
    private WorkiAnswerRepository answerRepository;

    @Mock
    private WorkiQuestionRepository questionRepository;

    @InjectMocks
    private WorkiAnswerService answerService;

    @Test
    @DisplayName("answering WAITING question marks it IN_PROGRESS")
    void createAnswer_onWaitingQuestion_marksInProgress() {
        Long authorId = 1001L;
        Long answererId = 1002L;
        Long questionId = 1010L;
        WorkiQuestion question = WorkiQuestion.create(authorId, "title", "content", null);
        when(questionRepository.findByQuestionIdAndDeletedAtIsNull(questionId))
                .thenReturn(Optional.of(question));
        when(answerRepository.save(any(WorkiAnswer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        answerService.createAnswer(answererId, questionId, new AnswerCreateRequest("answer content"));

        assertThat(question.getStatus()).isEqualTo(QuestionStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("answered question cannot receive another answer")
    void createAnswer_onAnsweredQuestion_throwsPolicyViolation() {
        Long authorId = 1001L;
        Long answererId = 1002L;
        Long questionId = 1010L;
        WorkiQuestion question = WorkiQuestion.create(authorId, "title", "content", null);
        question.markAnswered();
        when(questionRepository.findByQuestionIdAndDeletedAtIsNull(questionId))
                .thenReturn(Optional.of(question));

        assertThatThrownBy(() ->
                answerService.createAnswer(answererId, questionId, new AnswerCreateRequest("answer")))
                .isInstanceOf(CustomException.class)
                .extracting("errorType").isEqualTo(ErrorType.WORKI_POLICY_VIOLATION);
        verify(answerRepository, never()).save(any());
    }

    @Test
    @DisplayName("missing question answer returns not found")
    void createAnswer_whenQuestionMissing_throwsNotFound() {
        Long answererId = 1002L;
        Long missingQuestionId = 1099L;
        when(questionRepository.findByQuestionIdAndDeletedAtIsNull(missingQuestionId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                answerService.createAnswer(answererId, missingQuestionId, new AnswerCreateRequest("answer")))
                .isInstanceOf(CustomException.class)
                .extracting("errorType").isEqualTo(ErrorType.WORKI_NOT_FOUND);
    }

    @Test
    @DisplayName("question author can accept answer")
    void acceptAnswer_byQuestionAuthor_succeeds() {
        Long authorId = 1001L;
        Long answererId = 1002L;
        Long questionId = 1010L;
        Long answerId = 1050L;
        WorkiAnswer answer = WorkiAnswer.create(questionId, answererId, "answer");
        WorkiQuestion question = WorkiQuestion.create(authorId, "title", "content", null);
        when(answerRepository.findByAnswerIdAndDeletedAtIsNull(answerId))
                .thenReturn(Optional.of(answer));
        when(questionRepository.findByQuestionIdAndDeletedAtIsNull(questionId))
                .thenReturn(Optional.of(question));

        AnswerResponse response = answerService.acceptAnswer(authorId, answerId);

        assertThat(response.accepted()).isTrue();
        assertThat(answer.isAccepted()).isTrue();
        assertThat(answer.getAcceptedAt()).isNotNull();
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.ANSWERED);
    }

    @Test
    @DisplayName("non-author cannot accept answer")
    void acceptAnswer_byNonAuthor_throwsAccessDenied() {
        Long authorId = 1001L;
        Long otherUserId = 1002L;
        Long answererId = 1003L;
        Long questionId = 1010L;
        Long answerId = 1050L;
        WorkiAnswer answer = WorkiAnswer.create(questionId, answererId, "answer");
        WorkiQuestion question = WorkiQuestion.create(authorId, "title", "content", null);
        when(answerRepository.findByAnswerIdAndDeletedAtIsNull(anyLong()))
                .thenReturn(Optional.of(answer));
        when(questionRepository.findByQuestionIdAndDeletedAtIsNull(questionId))
                .thenReturn(Optional.of(question));

        assertThatThrownBy(() -> answerService.acceptAnswer(otherUserId, answerId))
                .isInstanceOf(CustomException.class)
                .extracting("errorType").isEqualTo(ErrorType.WORKI_FORBIDDEN);
    }

    @Test
    @DisplayName("already answered question cannot accept another answer")
    void acceptAnswer_whenAlreadyAnswered_throwsPolicyViolation() {
        Long authorId = 1001L;
        Long answererId = 1002L;
        Long questionId = 1010L;
        Long answerId = 1050L;
        WorkiAnswer answer = WorkiAnswer.create(questionId, answererId, "answer");
        WorkiQuestion question = WorkiQuestion.create(authorId, "title", "content", null);
        question.markAnswered();
        when(answerRepository.findByAnswerIdAndDeletedAtIsNull(anyLong()))
                .thenReturn(Optional.of(answer));
        when(questionRepository.findByQuestionIdAndDeletedAtIsNull(questionId))
                .thenReturn(Optional.of(question));

        assertThatThrownBy(() -> answerService.acceptAnswer(authorId, answerId))
                .isInstanceOf(CustomException.class)
                .extracting("errorType").isEqualTo(ErrorType.WORKI_POLICY_VIOLATION);
    }

    @Test
    @DisplayName("missing answer accept returns not found")
    void acceptAnswer_whenAnswerMissing_throwsNotFound() {
        Long authorId = 1001L;
        Long missingAnswerId = 1099L;
        when(answerRepository.findByAnswerIdAndDeletedAtIsNull(missingAnswerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> answerService.acceptAnswer(authorId, missingAnswerId))
                .isInstanceOf(CustomException.class)
                .extracting("errorType").isEqualTo(ErrorType.WORKI_NOT_FOUND);
    }
}
