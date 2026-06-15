package com.wip.workipedia.chatbot.dto;

import com.wip.workipedia.chatbot.domain.ChatbotMessage;
import com.wip.workipedia.chatbot.domain.NextAction;
import com.wip.workipedia.chatbot.domain.SenderType;
import java.time.LocalDateTime;

public record ChatbotMessageResponse(
        Long messageId,
        SenderType senderType,
        String content,
        Boolean answerable,
        NextAction nextAction,
        String referencesJson,
        LocalDateTime createdAt
) {
    public static ChatbotMessageResponse from(ChatbotMessage message) {
        return new ChatbotMessageResponse(
                message.getMessageId(),
                message.getSenderType(),
                message.getContent(),
                message.getAnswerable(),
                message.getNextAction(),
                message.getReferencesJson(),
                message.getCreatedAt()
        );
    }
}
