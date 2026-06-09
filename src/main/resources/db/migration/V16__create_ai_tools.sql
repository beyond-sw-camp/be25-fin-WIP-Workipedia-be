-- Function Calling에 사용하는 HTTP API / DB Query Tool 정의
-- 고객사 데이터 자체는 저장하지 않고 호출 방법과 허용 범위만 관리한다.

CREATE TABLE ai_tools (
    ai_tool_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    tool_type VARCHAR(30) NOT NULL,

    endpoint_url VARCHAR(1000) NULL,
    http_method VARCHAR(10) NULL,
    datasource_key VARCHAR(100) NULL,
    query_template LONGTEXT NULL,

    parameters_schema JSON NOT NULL,
    response_schema JSON NULL,
    auth_type VARCHAR(30) NOT NULL DEFAULT 'NONE',
    credential_ref VARCHAR(255) NULL,
    timeout_ms INT NOT NULL DEFAULT 5000,
    max_result_count INT NOT NULL DEFAULT 100,
    approval_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    is_active CHAR(1) NOT NULL DEFAULT 'N',

    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N',
    modified_source VARCHAR(30) NULL,

    active_name VARCHAR(100)
        GENERATED ALWAYS AS (
            CASE
                WHEN deleted_at IS NULL AND is_deleted = 'N' THEN name
                ELSE NULL
            END
        ) STORED,

    CONSTRAINT fk_ai_tools_created_by
        FOREIGN KEY (created_by)
        REFERENCES users (user_id),
    CONSTRAINT fk_ai_tools_updated_by
        FOREIGN KEY (updated_by)
        REFERENCES users (user_id),

    CONSTRAINT ck_ai_tools_tool_type
        CHECK (tool_type IN ('HTTP_API', 'DB_QUERY')),
    CONSTRAINT ck_ai_tools_http_method
        CHECK (
            http_method IS NULL
            OR http_method IN ('GET', 'POST', 'PUT', 'PATCH', 'DELETE')
        ),
    CONSTRAINT ck_ai_tools_auth_type
        CHECK (auth_type IN ('NONE', 'API_KEY', 'BEARER_TOKEN', 'OAUTH2')),
    CONSTRAINT ck_ai_tools_approval_status
        CHECK (approval_status IN ('DRAFT', 'APPROVED', 'REJECTED')),
    CONSTRAINT ck_ai_tools_is_active
        CHECK (is_active IN ('Y', 'N')),
    CONSTRAINT ck_ai_tools_is_deleted
        CHECK (is_deleted IN ('Y', 'N')),
    CONSTRAINT ck_ai_tools_timeout_ms
        CHECK (timeout_ms BETWEEN 100 AND 60000),
    CONSTRAINT ck_ai_tools_max_result_count
        CHECK (max_result_count BETWEEN 1 AND 1000),
    CONSTRAINT ck_ai_tools_type_configuration
        CHECK (
            (
                tool_type = 'HTTP_API'
                AND endpoint_url IS NOT NULL
                AND http_method IS NOT NULL
                AND datasource_key IS NULL
                AND query_template IS NULL
            )
            OR
            (
                tool_type = 'DB_QUERY'
                AND endpoint_url IS NULL
                AND http_method IS NULL
                AND datasource_key IS NOT NULL
                AND query_template IS NOT NULL
            )
        )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE UNIQUE INDEX uk_ai_tools_active_name
    ON ai_tools (active_name);

CREATE INDEX idx_ai_tools_type_active
    ON ai_tools (tool_type, is_active, approval_status);
