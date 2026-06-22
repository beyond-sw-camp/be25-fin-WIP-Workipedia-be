-- V39__create_tool_execution_logs.sql
-- Tool 실행 감사 로그. credential과 Tool 결과 원문은 기록하지 않고 마스킹된 파라미터만 남긴다.
CREATE TABLE tool_execution_logs (
    tool_execution_log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ai_tool_id BIGINT NOT NULL,
    caller VARCHAR(100) NOT NULL,
    masked_parameters JSON NULL,
    result_count INT NULL,
    duration_ms BIGINT NOT NULL,
    success CHAR(1) NOT NULL,
    error_code VARCHAR(50) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_tool_execution_logs_ai_tool
        FOREIGN KEY (ai_tool_id) REFERENCES ai_tools (ai_tool_id),
    CONSTRAINT ck_tool_execution_logs_success
        CHECK (success IN ('Y', 'N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_tool_execution_logs_ai_tool_id
    ON tool_execution_logs (ai_tool_id, created_at);
