package com.wip.workipedia.worki.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "worki_questions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkiQuestion extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long questionId;

    // author는 users 엔티티(이슬이 담당)와 연결하지 않고 식별자만 보관해 디커플링한다.
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private QuestionStatus status;

    @Column(name = "view_count", nullable = false)
    private long viewCount;

    @Column(name = "like_count", nullable = false)
    private long likeCount;

    @Column(name = "source_chatbot_message_id")
    private Long sourceChatbotMessageId;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private WorkiQuestion(Long userId, String title, String content, Long sourceChatbotMessageId) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.sourceChatbotMessageId = sourceChatbotMessageId;
        this.status = QuestionStatus.WAITING;
        this.viewCount = 0L;
        this.likeCount = 0L;
    }

    public static WorkiQuestion create(Long userId, String title, String content, Long sourceChatbotMessageId) {
        return new WorkiQuestion(userId, title, content, sourceChatbotMessageId);
    }

    public void updateContent(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void markInProgress() {
        if (this.status == QuestionStatus.WAITING) {
            this.status = QuestionStatus.IN_PROGRESS;
        }
    }

    public void markAnswered() {
        this.status = QuestionStatus.ANSWERED;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public boolean isWaiting() {
        return this.status == QuestionStatus.WAITING;
    }

    public boolean isAnswered() {
        return this.status == QuestionStatus.ANSWERED;
    }

    public boolean isAuthor(Long userId) {
        return this.userId.equals(userId);
    }
}
