package com.wip.workipedia.faq.dto;

import com.wip.workipedia.worki.repository.PopularWorkiProjection;
import java.time.LocalDateTime;

public record PopularWorkiResponse(
        Long questionId,
        String title,
        long likeCount,
        long viewCount,
        LocalDateTime createdAt
) {
    public static PopularWorkiResponse from(PopularWorkiProjection projection) {
        return new PopularWorkiResponse(
                projection.getQuestionId(),
                projection.getTitle(),
                projection.getLikeCount() == null ? 0L : projection.getLikeCount(),
                projection.getViewCount() == null ? 0L : projection.getViewCount(),
                projection.getCreatedAt()
        );
    }
}
