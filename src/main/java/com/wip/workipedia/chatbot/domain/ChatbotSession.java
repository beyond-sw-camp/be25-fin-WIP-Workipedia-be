package com.wip.workipedia.chatbot.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chatbot_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatbotSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

    @Column(nullable = false)
    private Long userId;

    private String title;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    @Column(nullable = false, columnDefinition = "CHAR(1)")
    private String isDeleted = "N";

    public static ChatbotSession create(Long userId, String title) {
        ChatbotSession s = new ChatbotSession();
        s.userId = userId;
        s.title = title;
        s.createdAt = LocalDateTime.now();
        s.updatedAt = LocalDateTime.now();
        return s;
    }
}
