package com.wip.workipedia.admin.aiprompt.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ai_prompt_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiPromptSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ai_prompt_setting_id")
    private Long aiPromptSettingId;

    @Column(columnDefinition = "TEXT")
    private String customPrompt;

    @Column(nullable = false, columnDefinition = "CHAR(1)")
    private String isActive = "N";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String customPrompt, boolean active) {
        this.customPrompt = customPrompt;
        this.isActive = active ? "Y" : "N";
    }

    public boolean isActive() {
        return "Y".equals(this.isActive);
    }

    public String getActiveCustomPrompt() {
        return isActive() ? customPrompt : null;
    }
}
