package com.wip.workipedia.search.dto;

import com.wip.workipedia.search.document.WorkiQuestionDocument;
import java.time.LocalDateTime;

/** 워키 질문 검색 결과 1건. */
public record WorkiSearchResponse(
        Long questionId,
        String title,
        String status,
        long viewCount,
        LocalDateTime createdAt
) {
    public static WorkiSearchResponse from(WorkiQuestionDocument document) {
        return new WorkiSearchResponse(
                document.getQuestionId(),
                document.getTitle(),
                document.getStatus(),
                document.getViewCount(),
                document.getCreatedAt()
        );
    }
}
