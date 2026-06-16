package com.wip.workipedia.chatbot.dto;

import com.wip.workipedia.chatbot.domain.ChatbotSession;
import java.time.LocalDateTime;

public record ChatbotSessionResponse(
        Long sessionId,
        String title,
        LocalDateTime createdAt
) {
    public static ChatbotSessionResponse from(ChatbotSession session) {
        return new ChatbotSessionResponse(
                session.getSessionId(),
                session.getTitle(),
                session.getCreatedAt()
        );
    }
}
