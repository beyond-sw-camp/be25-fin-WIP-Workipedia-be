package com.wip.workipedia.worki.dto;

import com.wip.workipedia.worki.domain.QuestionStatus;
import com.wip.workipedia.worki.domain.WorkiQuestion;
import java.time.LocalDateTime;

public record QuestionSummaryResponse(
        Long questionId,
        Long userId,
        String title,
        QuestionStatus status,
        long viewCount,
        long likeCount,
        LocalDateTime createdAt
) {
    public static QuestionSummaryResponse from(WorkiQuestion question) {
        return new QuestionSummaryResponse(
                question.getQuestionId(),
                question.getUserId(),
                question.getTitle(),
                question.getStatus(),
                question.getViewCount(),
                question.getLikeCount(),
                question.getCreatedAt()
        );
    }
}
