package com.wip.workipedia.worki.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "worki_answers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkiAnswer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long answerId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_accepted", nullable = false)
    private boolean accepted;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private WorkiAnswer(Long questionId, Long userId, String content) {
        this.questionId = questionId;
        this.userId = userId;
        this.content = content;
        this.accepted = false;
    }

    public static WorkiAnswer create(Long questionId, Long userId, String content) {
        return new WorkiAnswer(questionId, userId, content);
    }

    public void accept() {
        this.accepted = true;
        this.acceptedAt = LocalDateTime.now();
    }
}
