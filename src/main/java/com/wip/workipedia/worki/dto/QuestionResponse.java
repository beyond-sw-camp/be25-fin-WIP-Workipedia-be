package com.wip.workipedia.worki.dto;

import com.wip.workipedia.worki.domain.QuestionStatus;
import com.wip.workipedia.worki.domain.WorkiQuestion;

public record QuestionResponse(
        Long questionId,
        Long authorId,
        String title,
        QuestionStatus status
) {
    public static QuestionResponse from(WorkiQuestion question) {
        return new QuestionResponse(
                question.getQuestionId(),
                question.getAuthorId(),
                question.getTitle(),
                question.getStatus()
        );
    }
}
