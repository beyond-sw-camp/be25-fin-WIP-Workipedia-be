CREATE TABLE esg_metrics_weekly (
    esg_metric_weekly_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    metric_week_start DATE NOT NULL,
    metric_week_end DATE NOT NULL,
    saved_work_minutes DECIMAL(12,2) NOT NULL DEFAULT 0,
    saved_work_hours DECIMAL(12,2) NOT NULL DEFAULT 0,
    electricity_saved_kwh DECIMAL(12,3) NOT NULL DEFAULT 0,
    co2_saved_kg DECIMAL(12,3) NOT NULL DEFAULT 0,
    cited_chatbot_answer_count BIGINT NOT NULL DEFAULT 0,
    calculation_basis_json JSON NOT NULL,
    calculated_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    modified_source VARCHAR(30) NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
    CONSTRAINT uk_esg_metrics_weekly_start
        UNIQUE (metric_week_start),
    CONSTRAINT ck_esg_metrics_weekly_period
        CHECK (metric_week_end >= metric_week_start),
    CONSTRAINT ck_esg_metrics_weekly_saved_minutes
        CHECK (saved_work_minutes >= 0),
    CONSTRAINT ck_esg_metrics_weekly_saved_hours
        CHECK (saved_work_hours >= 0),
    CONSTRAINT ck_esg_metrics_weekly_electricity
        CHECK (electricity_saved_kwh >= 0),
    CONSTRAINT ck_esg_metrics_weekly_co2
        CHECK (co2_saved_kg >= 0),
    CONSTRAINT ck_esg_metrics_weekly_cited_answers
        CHECK (cited_chatbot_answer_count >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_esg_metrics_weekly_period
    ON esg_metrics_weekly (metric_week_start, metric_week_end);

CREATE INDEX idx_esg_metrics_weekly_calculated_at
    ON esg_metrics_weekly (calculated_at);
