package com.wip.workipedia.worki.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.wip.workipedia.worki.domain.QuestionStatus;
import com.wip.workipedia.worki.domain.WorkiAnswer;
import com.wip.workipedia.worki.domain.WorkiQuestion;
import com.wip.workipedia.worki.dto.QuestionCreateRequest;
import com.wip.workipedia.worki.dto.QuestionDetailResponse;
import com.wip.workipedia.worki.dto.QuestionResponse;
import com.wip.workipedia.worki.dto.QuestionUpdateRequest;
import com.wip.workipedia.worki.exception.WorkiAccessDeniedException;
import com.wip.workipedia.worki.exception.WorkiNotFoundException;
import com.wip.workipedia.worki.exception.WorkiPolicyViolationException;
import com.wip.workipedia.worki.repository.WorkiAnswerRepository;
import com.wip.workipedia.worki.repository.WorkiQuestionRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkiQuestionServiceTest {

    @Mock
    private WorkiQuestionRepository questionRepository;

    @Mock
    private WorkiAnswerRepository answerRepository;

    @InjectMocks
    private WorkiQuestionService questionService;

    private static final Long AUTHOR_ID = 1L;
    private static final Long OTHER_ID = 2L;

    @Test
    @DisplayName("질문 등록 시 상태는 WAITING이다")
    void create_setsWaitingStatus() {
        when(questionRepository.save(any(WorkiQuestion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        QuestionResponse response = questionService.create(
                AUTHOR_ID, new QuestionCreateRequest("제목", "내용", null));

        assertThat(response.status()).isEqualTo(QuestionStatus.WAITING);
        assertThat(response.authorId()).isEqualTo(AUTHOR_ID);
    }

    @Test
    @DisplayName("WAITING 상태에서 작성자 본인은 질문을 수정할 수 있다")
    void update_byAuthorInWaiting_succeeds() {
        WorkiQuestion question = WorkiQuestion.create(AUTHOR_ID, "원래제목", "원래내용", null);
        when(questionRepository.findByQuestionIdAndDeletedAtIsNull(10L))
                .thenReturn(Optional.of(question));

        questionService.update(AUTHOR_ID, 10L, new QuestionUpdateRequest("새제목", "새내용"));

        assertThat(question.getTitle()).isEqualTo("새제목");
        assertThat(question.getContent()).isEqualTo("새내용");
    }

    @Test
    @DisplayName("작성자가 아니면 질문 수정은 거부된다")
    void update_byNonAuthor_throwsAccessDenied() {
        WorkiQuestion question = WorkiQuestion.create(AUTHOR_ID, "제목", "내용", null);
        when(questionRepository.findByQuestionIdAndDeletedAtIsNull(10L))
                .thenReturn(Optional.of(question));

        assertThatThrownBy(() ->
                questionService.update(OTHER_ID, 10L, new QuestionUpdateRequest("새제목", "새내용")))
                .isInstanceOf(WorkiAccessDeniedException.class);
    }

    @Test
    @DisplayName("WAITING 상태가 아니면 질문 수정은 거부된다")
    void update_whenNotWaiting_throwsPolicyViolation() {
        WorkiQuestion question = WorkiQuestion.create(AUTHOR_ID, "제목", "내용", null);
        question.markInProgress();
        when(questionRepository.findByQuestionIdAndDeletedAtIsNull(10L))
                .thenReturn(Optional.of(question));

        assertThatThrownBy(() ->
                questionService.update(AUTHOR_ID, 10L, new QuestionUpdateRequest("새제목", "새내용")))
                .isInstanceOf(WorkiPolicyViolationException.class);
    }

    @Test
    @DisplayName("존재하지 않는 질문 수정은 404다")
    void update_whenQuestionMissing_throwsNotFound() {
        when(questionRepository.findByQuestionIdAndDeletedAtIsNull(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                questionService.update(AUTHOR_ID, 99L, new QuestionUpdateRequest("새제목", "새내용")))
                .isInstanceOf(WorkiNotFoundException.class);
    }

    @Test
    @DisplayName("질문 상세 조회 시 조회수가 증가하고 답변이 함께 반환된다")
    void getDetail_incrementsViewCountAndReturnsAnswers() {
        WorkiQuestion question = WorkiQuestion.create(AUTHOR_ID, "제목", "내용", null);
        when(questionRepository.findByQuestionIdAndDeletedAtIsNull(10L))
                .thenReturn(Optional.of(question));
        when(answerRepository.findByQuestionIdAndDeletedAtIsNullOrderByCreatedAtAsc(10L))
                .thenReturn(List.of(WorkiAnswer.create(10L, OTHER_ID, "답변")));

        QuestionDetailResponse response = questionService.getDetail(10L);

        assertThat(question.getViewCount()).isEqualTo(1L);
        assertThat(response.answers()).hasSize(1);
    }
}
