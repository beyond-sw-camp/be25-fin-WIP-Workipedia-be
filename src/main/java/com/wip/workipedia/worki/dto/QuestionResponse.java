package com.wip.workipedia.worki.dto;

import com.wip.workipedia.worki.domain.QuestionStatus;
import com.wip.workipedia.worki.domain.WorkiQuestion;

public record QuestionResponse(
        Long questionId,
        Long userId,
        String title,
        QuestionStatus status,
        // TODO: users.nickname(이슬이) 통합 후 매핑. 현재는 null.
        String authorNickname
) {
    public static QuestionResponse from(WorkiQuestion question) {
        return new QuestionResponse(
                question.getQuestionId(),
                question.getUserId(),
                question.getTitle(),
                question.getStatus(),
                null
        );
    }
}
