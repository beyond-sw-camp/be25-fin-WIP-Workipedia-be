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

    // author는 users 엔티티(이슬이 담당)와 연결하지 않고 식별자만 보관해 디커플링.
    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "source_chatbot_message_id")
    private Long sourceChatbotMessageId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private QuestionStatus status;

    @Column(name = "accepted_answer_id")
    private Long acceptedAnswerId;

    @Column(name = "view_count", nullable = false)
    private long viewCount;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private WorkiQuestion(Long authorId, String title, String content, Long sourceChatbotMessageId) {
        this.authorId = authorId;
        this.title = title;
        this.content = content;
        this.sourceChatbotMessageId = sourceChatbotMessageId;
        this.status = QuestionStatus.WAITING;
        this.viewCount = 0L;
    }

    public static WorkiQuestion create(Long authorId, String title, String content, Long sourceChatbotMessageId) {
        return new WorkiQuestion(authorId, title, content, sourceChatbotMessageId);
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

    public void acceptAnswer(Long acceptedAnswerId) {
        this.acceptedAnswerId = acceptedAnswerId;
        markAnswered();
    }

    // 상태만 ANSWERED로 바꾸는 유틸리티(관리자 강제 변경/테스트 셋업).
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
        return this.authorId.equals(userId);
    }
}
