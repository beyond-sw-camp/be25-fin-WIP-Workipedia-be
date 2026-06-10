package com.wip.workipedia.flashchat.dto;

import java.time.LocalDateTime;

public record FlashChatMessageResponse(
        String id,
        Long userId,
        String nickname,
        String content,
        String replyToId,
        LocalDateTime createdAt,
        LocalDateTime expiresAt
) {}
