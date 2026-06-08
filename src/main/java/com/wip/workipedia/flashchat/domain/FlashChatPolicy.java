package com.wip.workipedia.flashchat.domain;

import com.wip.workipedia.common.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "flash_chat_policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FlashChatPolicy extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int messageTtlSeconds;

    @Column(nullable = false)
    private int sendCooldownSeconds;

    @Column(columnDefinition = "JSON")
    private String bannedWords;

    public void update(int messageTtlSeconds, int sendCooldownSeconds, String bannedWords) {
        this.messageTtlSeconds = messageTtlSeconds;
        this.sendCooldownSeconds = sendCooldownSeconds;
        this.bannedWords = bannedWords;
    }
}
