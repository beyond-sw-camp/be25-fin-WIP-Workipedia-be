-- 부서 역할 배정 프롬프트

CREATE TABLE department_routing_prompts (
    routing_prompt_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    department_id BIGINT NOT NULL,
    prompt_content TEXT NOT NULL, -- 프롬프트 내용
    is_active CHAR(1) NOT NULL DEFAULT 'Y' CHECK (is_active IN ('Y', 'N')),  -- 활성여부
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),

    CONSTRAINT fk_department_routing_prompts_department
        FOREIGN KEY (department_id)
        REFERENCES departments (department_id),

    CONSTRAINT fk_department_routing_prompts_created_by
        FOREIGN KEY (created_by)
        REFERENCES users (user_id),

    CONSTRAINT fk_department_routing_prompts_updated_by
        FOREIGN KEY (updated_by)
        REFERENCES users (user_id),

    CONSTRAINT uk_department_routing_prompts_department
        UNIQUE (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_department_routing_prompts_department_id
ON department_routing_prompts (department_id);


-- 키워드별 배정 규칙
CREATE TABLE routing_rules (
    rule_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL, -- 규칙 이름
    keywords VARCHAR(500) NOT NULL,  -- 규칙 추가할 키워드
    target_department_id BIGINT NOT NULL,   -- 배정 부서
    active BOOLEAN NOT NULL DEFAULT TRUE,   -- 활성여부

    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),

    CONSTRAINT fk_routing_rules_target_department
        FOREIGN KEY (target_department_id)
        REFERENCES departments (department_id),

    CONSTRAINT fk_routing_rules_created_by
        FOREIGN KEY (created_by)
        REFERENCES users (user_id),

    CONSTRAINT fk_routing_rules_updated_by
        FOREIGN KEY (updated_by)
        REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_routing_rules_target_department_id
ON routing_rules (target_department_id);

CREATE INDEX idx_routing_rules_active
ON routing_rules (active);
