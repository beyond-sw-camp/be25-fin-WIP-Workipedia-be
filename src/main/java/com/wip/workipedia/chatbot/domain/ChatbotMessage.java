package com.wip.workipedia.chatbot.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chatbot_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatbotMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    @Column(nullable = false)
    private Long sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SenderType senderType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private Boolean answerable;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private NextAction nextAction;

    @Column(columnDefinition = "JSON")
    private String referencesJson;

    private Long sourceWorkiQuestionId;
    private Long sourceTicketId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    @Column(nullable = false, columnDefinition = "CHAR(1)")
    private String isDeleted = "N";

    public static ChatbotMessage ofUser(Long sessionId, String content) {
        ChatbotMessage m = new ChatbotMessage();
        m.sessionId = sessionId;
        m.senderType = SenderType.USER;
        m.content = content;
        m.createdAt = LocalDateTime.now();
        m.updatedAt = LocalDateTime.now();
        return m;
    }

    public static ChatbotMessage ofAssistant(Long sessionId, String content,
            Boolean answerable, NextAction nextAction, String referencesJson) {
        ChatbotMessage m = new ChatbotMessage();
        m.sessionId = sessionId;
        m.senderType = SenderType.ASSISTANT;
        m.content = content;
        m.answerable = answerable;
        m.nextAction = nextAction;
        m.referencesJson = referencesJson;
        m.createdAt = LocalDateTime.now();
        m.updatedAt = LocalDateTime.now();
        return m;
    }

    public static ChatbotMessage ofSystem(Long sessionId, String content) {
        ChatbotMessage m = new ChatbotMessage();
        m.sessionId = sessionId;
        m.senderType = SenderType.SYSTEM;
        m.content = content;
        m.createdAt = LocalDateTime.now();
        m.updatedAt = LocalDateTime.now();
        return m;
    }
}
