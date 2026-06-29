-- ERP 부서 원본 스테이징 (운영 departments와 분리)
CREATE TABLE external_departments (
    external_department_id BIGINT       NOT NULL AUTO_INCREMENT,
    source_system          VARCHAR(50)  NOT NULL,
    external_id            VARCHAR(100) NOT NULL,
    department_name        VARCHAR(100) NOT NULL,
    parent_external_id     VARCHAR(100) NULL,
    duty_desc              TEXT         NULL,
    use_yn                 CHAR(1)      NOT NULL DEFAULT 'Y' CHECK (use_yn IN ('Y','N')),
    raw_payload            JSON         NULL,
    mapped_department_id   BIGINT       NULL,
    sync_state             VARCHAR(20)  NOT NULL DEFAULT 'NEW'
        CHECK (sync_state IN ('NEW','MATCHED','RENAMED','MERGED','DELETED','APPLIED')),
    fetched_at             DATETIME     NOT NULL,
    applied_at             DATETIME     NULL,
    created_at             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (external_department_id),
    UNIQUE KEY uk_external_departments_source_external (source_system, external_id),
    KEY idx_external_departments_mapped_dept (mapped_department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
