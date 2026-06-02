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

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    // 공식 답변(티켓에서 채택된 답변)에 연결될 수 있는 티켓 식별자. 일반 답변은 NULL.
    @Column(name = "ticket_id")
    private Long ticketId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "official", nullable = false)
    private boolean official;

    @Column(name = "accepted", nullable = false)
    private boolean accepted;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private WorkiAnswer(Long questionId, Long authorId, String content) {
        this.questionId = questionId;
        this.authorId = authorId;
        this.content = content;
        this.official = false;
        this.accepted = false;
    }

    public static WorkiAnswer create(Long questionId, Long authorId, String content) {
        return new WorkiAnswer(questionId, authorId, content);
    }

    public void accept() {
        this.accepted = true;
        this.acceptedAt = LocalDateTime.now();
    }
}
