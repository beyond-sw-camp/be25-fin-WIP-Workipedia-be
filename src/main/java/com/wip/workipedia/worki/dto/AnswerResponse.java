package com.wip.workipedia.worki.dto;

import com.wip.workipedia.worki.domain.WorkiAnswer;
import java.time.LocalDateTime;

public record AnswerResponse(
        Long answerId,
        Long questionId,
        Long authorId,
        String content,
        boolean accepted,
        boolean official,
        LocalDateTime acceptedAt,
        LocalDateTime createdAt
) {
    public static AnswerResponse from(WorkiAnswer answer) {
        return new AnswerResponse(
                answer.getAnswerId(),
                answer.getQuestionId(),
                answer.getAuthorId(),
                answer.getContent(),
                answer.isAccepted(),
                answer.isOfficial(),
                answer.getAcceptedAt(),
                answer.getCreatedAt()
        );
    }
}
