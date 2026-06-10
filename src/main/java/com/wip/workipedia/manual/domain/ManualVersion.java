package com.wip.workipedia.manual.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "manual_versions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ManualVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manual_version_id")
    private Long manualVersionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manual_id", nullable = false)
    private Manual manual;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "manual_num", nullable = false, length = 50)
    private String manualNum;

    @Column(name = "update_reason", nullable = false, columnDefinition = "TEXT")
    private String updateReason;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private ManualStatus status;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "version", length = 50)
    private String version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private ManualVersion(Manual manual, Long userId, String manualNum, String updateReason) {
        this.manual = manual;
        this.userId = userId;
        this.manualNum = manualNum;
        this.updateReason = updateReason;
        this.title = manual.getTitle();
        this.content = manual.getContent();
        this.status = manual.getStatus();
        this.sourceUrl = manual.getSourceUrl();
        this.version = manual.getVersion();
    }

    public static ManualVersion create(Manual manual, Long userId, String manualNum, String updateReason) {
        return new ManualVersion(manual, userId, manualNum, updateReason);
    }

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
}
