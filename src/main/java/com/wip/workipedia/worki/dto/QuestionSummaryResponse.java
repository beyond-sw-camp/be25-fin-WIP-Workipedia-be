package com.wip.workipedia.worki.dto;

import com.wip.workipedia.worki.domain.QuestionStatus;
import com.wip.workipedia.worki.domain.WorkiQuestion;
import java.time.LocalDateTime;

public record QuestionSummaryResponse(
        Long questionId,
        Long authorId,
        String title,
        QuestionStatus status,
        long viewCount,
        LocalDateTime createdAt
) {
    public static QuestionSummaryResponse from(WorkiQuestion question) {
        return new QuestionSummaryResponse(
                question.getQuestionId(),
                question.getAuthorId(),
                question.getTitle(),
                question.getStatus(),
                question.getViewCount(),
                question.getCreatedAt()
        );
    }
}
