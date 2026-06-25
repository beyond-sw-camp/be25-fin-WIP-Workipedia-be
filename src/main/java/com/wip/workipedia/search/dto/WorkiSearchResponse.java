package com.wip.workipedia.search.dto;

import com.wip.workipedia.search.document.WorkiQuestionDocument;
import java.time.LocalDateTime;

/** 워키 질문 검색 결과 1건. */
public record WorkiSearchResponse(
        Long questionId,
        String title,
        String content,
        String status,
        long viewCount,
        long answerCount,
        LocalDateTime createdAt
) {
    public static WorkiSearchResponse from(WorkiQuestionDocument document, long answerCount) {
        return new WorkiSearchResponse(
                document.getQuestionId(),
                document.getTitle(),
                document.getContent(),
                document.getStatus(),
                document.getViewCount(),
                answerCount,
                document.getCreatedAt()
        );
    }
}
