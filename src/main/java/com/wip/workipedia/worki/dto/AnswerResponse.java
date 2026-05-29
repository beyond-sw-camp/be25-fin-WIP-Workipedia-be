package com.wip.workipedia.worki.dto;

import com.wip.workipedia.worki.domain.WorkiAnswer;
import java.time.LocalDateTime;

public record AnswerResponse(
        Long answerId,
        Long questionId,
        Long userId,
        String content,
        boolean accepted,
        LocalDateTime acceptedAt,
        LocalDateTime createdAt
) {
    public static AnswerResponse from(WorkiAnswer answer) {
        return new AnswerResponse(
                answer.getAnswerId(),
                answer.getQuestionId(),
                answer.getUserId(),
                answer.getContent(),
                answer.isAccepted(),
                answer.getAcceptedAt(),
                answer.getCreatedAt()
        );
    }
}
