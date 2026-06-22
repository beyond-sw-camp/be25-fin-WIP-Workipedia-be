package com.wip.workipedia.aisync.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_sync_cleanup_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiSyncCleanupLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ai_sync_cleanup_log_id")
    private Long aiSyncCleanupLogId;

    @Enumerated(EnumType.STRING)
    @Column(name = "triggered_by", nullable = false, length = 10)
    private CleanupTrigger triggeredBy;

    @Column(name = "deleted_count", nullable = false)
    private int deletedCount;

    @Column(name = "skipped_count", nullable = false)
    private int skippedCount;

    @Column(name = "failed_count", nullable = false)
    private int failedCount;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "CHAR(1)")
    private String isDeleted = "N";

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

    public static AiSyncCleanupLog of(CleanupTrigger trigger, int deleted, int skipped, int failed) {
        AiSyncCleanupLog log = new AiSyncCleanupLog();
        log.triggeredBy = trigger;
        log.deletedCount = deleted;
        log.skippedCount = skipped;
        log.failedCount = failed;
        log.completedAt = LocalDateTime.now();
        return log;
    }
}
