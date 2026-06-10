package com.wip.workipedia.worki.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.worki.domain.QuestionStatus;
import com.wip.workipedia.worki.domain.WorkiAnswer;
import com.wip.workipedia.worki.domain.WorkiQuestion;
import com.wip.workipedia.worki.dto.QuestionCreateRequest;
import com.wip.workipedia.worki.dto.QuestionDetailResponse;
import com.wip.workipedia.worki.dto.QuestionResponse;
import com.wip.workipedia.worki.dto.QuestionUpdateRequest;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.user.repository.UserRepository;
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

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WorkiQuestionService questionService;

    @Test
    @DisplayName("create question sets WAITING status")
    void create_setsWaitingStatus() {
        Long authorId = 1001L;
        when(questionRepository.save(any(WorkiQuestion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        QuestionResponse response = questionService.create(
                authorId, new QuestionCreateRequest("title", "content", null));

        assertThat(response.status()).isEqualTo(QuestionStatus.WAITING);
        assertThat(response.authorId()).isEqualTo(authorId);
    }

    @Test
    @DisplayName("author can update WAITING question")
    void update_byAuthorInWaiting_succeeds() {
        Long authorId = 1001L;
        Long questionId = 1010L;
        WorkiQuestion question = WorkiQuestion.create(authorId, "old title", "old content", null);
        when(questionRepository.findByQuestionIdAndDeletedAtIsNull(questionId))
                .thenReturn(Optional.of(question));

        questionService.update(authorId, questionId, new QuestionUpdateRequest("new title", "new content"));

        assertThat(question.getTitle()).isEqualTo("new title");
        assertThat(question.getContent()).isEqualTo("new content");
    }

    @Test
    @DisplayName("non-author cannot update question")
    void update_byNonAuthor_throwsAccessDenied() {
        Long authorId = 1001L;
        Long otherUserId = 1002L;
        Long questionId = 1010L;
        WorkiQuestion question = WorkiQuestion.create(authorId, "title", "content", null);
        when(questionRepository.findByQuestionIdAndDeletedAtIsNull(questionId))
                .thenReturn(Optional.of(question));

        assertThatThrownBy(() ->
                questionService.update(otherUserId, questionId, new QuestionUpdateRequest("new title", "new content")))
                .isInstanceOf(CustomException.class)
                .extracting("errorType").isEqualTo(ErrorType.WORKI_FORBIDDEN);
    }

    @Test
    @DisplayName("non-WAITING question cannot be updated")
    void update_whenNotWaiting_throwsPolicyViolation() {
        Long authorId = 1001L;
        Long questionId = 1010L;
        WorkiQuestion question = WorkiQuestion.create(authorId, "title", "content", null);
        question.markInProgress();
        when(questionRepository.findByQuestionIdAndDeletedAtIsNull(questionId))
                .thenReturn(Optional.of(question));

        assertThatThrownBy(() ->
                questionService.update(authorId, questionId, new QuestionUpdateRequest("new title", "new content")))
                .isInstanceOf(CustomException.class)
                .extracting("errorType").isEqualTo(ErrorType.WORKI_POLICY_VIOLATION);
    }

    @Test
    @DisplayName("missing question update returns not found")
    void update_whenQuestionMissing_throwsNotFound() {
        Long authorId = 1001L;
        Long missingQuestionId = 1099L;
        when(questionRepository.findByQuestionIdAndDeletedAtIsNull(missingQuestionId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                questionService.update(authorId, missingQuestionId, new QuestionUpdateRequest("new title", "new content")))
                .isInstanceOf(CustomException.class)
                .extracting("errorType").isEqualTo(ErrorType.WORKI_NOT_FOUND);
    }

    @Test
    @DisplayName("detail increments view count and returns answers")
    void getDetail_incrementsViewCountAndReturnsAnswers() {
        Long authorId = 1001L;
        Long answererId = 1002L;
        Long questionId = 1010L;
        WorkiQuestion question = WorkiQuestion.create(authorId, "title", "content", null);
        when(questionRepository.findByQuestionIdAndDeletedAtIsNull(questionId))
                .thenReturn(Optional.of(question));
        when(answerRepository.findByQuestionIdAndDeletedAtIsNullOrderByCreatedAtAsc(questionId))
                .thenReturn(List.of(WorkiAnswer.create(questionId, answererId, "answer")));

        QuestionDetailResponse response = questionService.getDetail(questionId);

        long expectedViewCount = 1L;
        assertThat(question.getViewCount()).isEqualTo(expectedViewCount);
        assertThat(response.answers()).hasSize(1);
    }
}
