package com.wip.workipedia.admin.flashchat.dto;

import java.util.List;

public record FlashChatPolicyResponse(
        int messageTtlSeconds,
        int sendCooldownSeconds,
        List<String> bannedWords
) {}
