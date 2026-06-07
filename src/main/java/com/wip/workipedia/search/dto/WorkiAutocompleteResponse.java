package com.wip.workipedia.search.dto;

import com.wip.workipedia.worki.domain.WorkiQuestion;

/** 검색어 자동완성 추천 1건 (질문 제목 기반). */
public record WorkiAutocompleteResponse(
        Long questionId,
        String title
) {
    public static WorkiAutocompleteResponse from(WorkiQuestion question) {
        return new WorkiAutocompleteResponse(
                question.getQuestionId(),
                question.getTitle()
        );
    }
}
