CREATE TABLE llm_usage_metrics (
    llm_usage_metric_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chatbot_message_id BIGINT NOT NULL,
    provider VARCHAR(50) NULL,
    model VARCHAR(100) NULL,
    question_snapshot VARCHAR(500) NULL,
    answerable CHAR(1) NOT NULL DEFAULT 'Y' CHECK (answerable IN ('Y', 'N')),
    prompt_tokens BIGINT NULL,
    completion_tokens BIGINT NULL,
    total_tokens BIGINT NULL,
    source_count INT NOT NULL DEFAULT 0,
    cited_chunk_count INT NOT NULL DEFAULT 0,
    full_file_tokens BIGINT NOT NULL DEFAULT 0,
    rag_tokens BIGINT NOT NULL DEFAULT 0,
    saved_tokens BIGINT NOT NULL DEFAULT 0,
    reduction_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    full_file_credits BIGINT NOT NULL DEFAULT 0,
    rag_credits BIGINT NOT NULL DEFAULT 0,
    saved_credits BIGINT NOT NULL DEFAULT 0,
    full_file_calls INT NOT NULL DEFAULT 0,
    rag_calls INT NOT NULL DEFAULT 0,
    saved_calls INT NOT NULL DEFAULT 0,
    source_breakdown JSON NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
    CONSTRAINT uk_llm_usage_metrics_chatbot_message
        UNIQUE (chatbot_message_id),
    CONSTRAINT fk_llm_usage_metrics_chatbot_message
        FOREIGN KEY (chatbot_message_id)
        REFERENCES chatbot_messages (message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_llm_usage_metrics_created_at
    ON llm_usage_metrics (created_at);

CREATE INDEX idx_llm_usage_metrics_deleted_created
    ON llm_usage_metrics (is_deleted, created_at);
