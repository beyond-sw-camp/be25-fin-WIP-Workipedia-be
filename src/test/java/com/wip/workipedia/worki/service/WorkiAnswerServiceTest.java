package com.wip.workipedia.worki.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wip.workipedia.worki.domain.QuestionStatus;
import com.wip.workipedia.worki.domain.WorkiAnswer;
import com.wip.workipedia.worki.domain.WorkiQuestion;
import com.wip.workipedia.worki.dto.AnswerCreateRequest;
import com.wip.workipedia.worki.dto.AnswerResponse;
import com.wip.workipedia.worki.exception.WorkiAccessDeniedException;
import com.wip.workipedia.worki.exception.WorkiNotFoundException;
import com.wip.workipedia.worki.exception.WorkiPolicyViolationException;
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

    private static final Long AUTHOR_ID = 1L;
    private static final Long ANSWERER_ID = 2L;
    private static final Long QUESTION_ID = 10L;

    @Test
    @DisplayName("WAITING 질문에 답변을 등록하면 질문 상태가 IN_PROGRESS로 전이된다")
    void createAnswer_onWaitingQuestion_marksInProgress() {
        WorkiQuestion question = WorkiQuestion.create(AUTHOR_ID, "제목", "내용", null);
        when(questionRepository.findByQuestionIdAndDeletedAtIsNull(QUESTION_ID))
                .thenReturn(Optional.of(question));
        when(answerRepository.save(any(WorkiAnswer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        answerService.createAnswer(ANSWERER_ID, QUESTION_ID, new AnswerCreateRequest("답변 내용"));

        assertThat(question.getStatus()).isEqualTo(QuestionStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("채택된 답변이 있는 질문에는 추가 답변을 등록할 수 없다")
    void createAnswer_onAnsweredQuestion_throwsPolicyViolation() {
        WorkiQuestion question = WorkiQuestion.create(AUTHOR_ID, "제목", "내용", null);
        question.markAnswered();
        when(questionRepository.findByQuestionIdAndDeletedAtIsNull(QUESTION_ID))
                .thenReturn(Optional.of(question));

        assertThatThrownBy(() ->
                answerService.createAnswer(ANSWERER_ID, QUESTION_ID, new AnswerCreateRequest("답변")))
                .isInstanceOf(WorkiPolicyViolationException.class);
        verify(answerRepository, never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 질문에 답변 등록은 404다")
    void createAnswer_whenQuestionMissing_throwsNotFound() {
        when(questionRepository.findByQuestionIdAndDeletedAtIsNull(QUESTION_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                answerService.createAnswer(ANSWERER_ID, QUESTION_ID, new AnswerCreateRequest("답변")))
                .isInstanceOf(WorkiNotFoundException.class);
    }

    @Test
    @DisplayName("질문 작성자가 답변을 채택하면 답변이 채택되고 질문은 ANSWERED가 된다")
    void acceptAnswer_byQuestionAuthor_succeeds() {
        WorkiAnswer answer = WorkiAnswer.create(QUESTION_ID, ANSWERER_ID, "답변");
        WorkiQuestion question = WorkiQuestion.create(AUTHOR_ID, "제목", "내용", null);
        when(answerRepository.findByAnswerIdAndDeletedAtIsNull(5L))
                .thenReturn(Optional.of(answer));
        when(questionRepository.findByQuestionIdAndDeletedAtIsNull(QUESTION_ID))
                .thenReturn(Optional.of(question));

        AnswerResponse response = answerService.acceptAnswer(AUTHOR_ID, 5L);

        assertThat(response.accepted()).isTrue();
        assertThat(answer.isAccepted()).isTrue();
        assertThat(answer.getAcceptedAt()).isNotNull();
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.ANSWERED);
    }

    @Test
    @DisplayName("질문 작성자가 아니면 답변 채택은 거부된다")
    void acceptAnswer_byNonAuthor_throwsAccessDenied() {
        WorkiAnswer answer = WorkiAnswer.create(QUESTION_ID, ANSWERER_ID, "답변");
        WorkiQuestion question = WorkiQuestion.create(AUTHOR_ID, "제목", "내용", null);
        when(answerRepository.findByAnswerIdAndDeletedAtIsNull(anyLong()))
                .thenReturn(Optional.of(answer));
        when(questionRepository.findByQuestionIdAndDeletedAtIsNull(QUESTION_ID))
                .thenReturn(Optional.of(question));

        assertThatThrownBy(() -> answerService.acceptAnswer(99L, 5L))
                .isInstanceOf(WorkiAccessDeniedException.class);
    }

    @Test
    @DisplayName("이미 채택된 답변이 있으면 추가 채택은 거부된다")
    void acceptAnswer_whenAlreadyAnswered_throwsPolicyViolation() {
        WorkiAnswer answer = WorkiAnswer.create(QUESTION_ID, ANSWERER_ID, "답변");
        WorkiQuestion question = WorkiQuestion.create(AUTHOR_ID, "제목", "내용", null);
        question.markAnswered();
        when(answerRepository.findByAnswerIdAndDeletedAtIsNull(anyLong()))
                .thenReturn(Optional.of(answer));
        when(questionRepository.findByQuestionIdAndDeletedAtIsNull(QUESTION_ID))
                .thenReturn(Optional.of(question));

        assertThatThrownBy(() -> answerService.acceptAnswer(AUTHOR_ID, 5L))
                .isInstanceOf(WorkiPolicyViolationException.class);
    }

    @Test
    @DisplayName("존재하지 않는 답변 채택은 404다")
    void acceptAnswer_whenAnswerMissing_throwsNotFound() {
        when(answerRepository.findByAnswerIdAndDeletedAtIsNull(5L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> answerService.acceptAnswer(AUTHOR_ID, 5L))
                .isInstanceOf(WorkiNotFoundException.class);
    }
}
