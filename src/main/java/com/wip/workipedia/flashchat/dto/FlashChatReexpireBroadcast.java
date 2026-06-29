package com.wip.workipedia.flashchat.dto;

// 정책 TTL 변경 시 현재 활성 메시지 전체의 만료 시각을 일괄 갱신하라는 브로드캐스트.
// 옵션 B(now + 새 TTL)라 모든 메시지가 동일한 expiresAt을 갖는다.
public record FlashChatReexpireBroadcast(
        String type,
        String expiresAt
) {
    public static FlashChatReexpireBroadcast of(String expiresAt) {
        return new FlashChatReexpireBroadcast("REEXPIRE", expiresAt);
    }
}
