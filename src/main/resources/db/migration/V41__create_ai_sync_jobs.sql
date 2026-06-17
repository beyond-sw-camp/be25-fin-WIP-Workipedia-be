CREATE TABLE ai_sync_jobs (
    ai_sync_job_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_type       VARCHAR(30)  NOT NULL COMMENT 'MANUAL, WORKI, KNOWLEDGE_DATA, MANUAL_KNOWLEDGE, DEPT_RR',
    source_id         BIGINT       NOT NULL,
    operation         VARCHAR(10)  NOT NULL COMMENT 'UPSERT, DELETE',
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, PROCESSING, SYNCED, FAILED',
    retry_count       INT          NOT NULL DEFAULT 0,
    last_error        TEXT,
    next_retry_at     DATETIME,
    lease_expires_at  DATETIME,
    started_at        DATETIME,
    completed_at      DATETIME,
    created_at        DATETIME     NOT NULL,
    updated_at        DATETIME     NOT NULL,
    deleted_at        DATETIME,
    modified_source   VARCHAR(30)
);

CREATE INDEX idx_ai_sync_jobs_status_next_retry
    ON ai_sync_jobs (status, next_retry_at);

CREATE INDEX idx_ai_sync_jobs_source
    ON ai_sync_jobs (source_type, source_id, created_at);
