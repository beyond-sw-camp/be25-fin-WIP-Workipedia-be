CREATE TABLE ai_sync_settings (
    ai_sync_setting_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    retention_days     INT     NOT NULL DEFAULT 365,
    created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME NULL     ON UPDATE CURRENT_TIMESTAMP,
    deleted_at         DATETIME NULL,
    is_deleted         CHAR(1)  NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO ai_sync_settings (retention_days)
VALUES (365);

CREATE TABLE ai_sync_cleanup_logs (
    ai_sync_cleanup_log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    triggered_by           VARCHAR(10) NOT NULL,
    deleted_count          INT         NOT NULL DEFAULT 0,
    skipped_count          INT         NOT NULL DEFAULT 0,
    failed_count           INT         NOT NULL DEFAULT 0,
    completed_at           DATETIME    NOT NULL,
    created_at             DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME    NULL     ON UPDATE CURRENT_TIMESTAMP,
    deleted_at             DATETIME    NULL,
    is_deleted             CHAR(1)     NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
    CONSTRAINT ck_ai_sync_cleanup_logs_triggered_by CHECK (triggered_by IN ('SCHEDULE', 'MANUAL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
