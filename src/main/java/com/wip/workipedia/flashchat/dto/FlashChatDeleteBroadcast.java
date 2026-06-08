package com.wip.workipedia.flashchat.dto;

public record FlashChatDeleteBroadcast(
        String type,
        String id
) {
    public static FlashChatDeleteBroadcast of(String id) {
        return new FlashChatDeleteBroadcast("DELETE", id);
    }
}
