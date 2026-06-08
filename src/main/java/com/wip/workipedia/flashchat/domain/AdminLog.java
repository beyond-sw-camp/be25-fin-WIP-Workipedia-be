package com.wip.workipedia.flashchat.domain;

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

@Entity
@Table(name = "admin_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_log_id")
    private Long adminLogId;

    @Column(nullable = false)
    private Long actorId;

    @Column(nullable = false, length = 50)
    private String actionType;

    @Column(length = 50)
    private String targetType;

    @Column(length = 1000)
    private String description;

    @Column(columnDefinition = "JSON")
    private String metadataJson;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, columnDefinition = "CHAR(1)")
    private String isDeleted = "N";

    public static AdminLog of(Long actorId, String actionType, String targetType,
                              String description, String metadataJson) {
        AdminLog log = new AdminLog();
        log.actorId = actorId;
        log.actionType = actionType;
        log.targetType = targetType;
        log.description = description;
        log.metadataJson = metadataJson;
        log.createdAt = LocalDateTime.now();
        return log;
    }
}
