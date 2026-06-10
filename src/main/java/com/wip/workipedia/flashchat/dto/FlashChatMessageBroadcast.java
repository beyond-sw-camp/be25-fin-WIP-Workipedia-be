package com.wip.workipedia.flashchat.dto;

import java.time.LocalDateTime;

public record FlashChatMessageBroadcast(
        String type,
        String id,
        Long userId,
        String nickname,
        String content,
        String replyToId,
        LocalDateTime createdAt,
        LocalDateTime expiresAt
) {
    public static FlashChatMessageBroadcast of(String id, Long userId, String nickname,
                                               String content, String replyToId,
                                               LocalDateTime createdAt, LocalDateTime expiresAt) {
        return new FlashChatMessageBroadcast("MESSAGE", id, userId, nickname, content,
                replyToId, createdAt, expiresAt);
    }
}
