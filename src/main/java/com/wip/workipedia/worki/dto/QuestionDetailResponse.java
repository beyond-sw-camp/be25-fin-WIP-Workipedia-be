package com.wip.workipedia.worki.dto;

import com.wip.workipedia.worki.domain.QuestionStatus;
import com.wip.workipedia.worki.domain.WorkiAnswer;
import com.wip.workipedia.worki.domain.WorkiQuestion;
import java.time.LocalDateTime;
import java.util.List;

public record QuestionDetailResponse(
        Long questionId,
        Long authorId,
        String title,
        String content,
        QuestionStatus status,
        Long acceptedAnswerId,
        long viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<AnswerResponse> answers
) {
    public static QuestionDetailResponse of(WorkiQuestion question, List<WorkiAnswer> answers) {
        return new QuestionDetailResponse(
                question.getQuestionId(),
                question.getAuthorId(),
                question.getTitle(),
                question.getContent(),
                question.getStatus(),
                question.getAcceptedAnswerId(),
                question.getViewCount(),
                question.getCreatedAt(),
                question.getUpdatedAt(),
                answers.stream().map(AnswerResponse::from).toList()
        );
    }
}
