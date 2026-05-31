CREATE TABLE badges (
    badge_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    CONSTRAINT uk_badges_code UNIQUE (code),
    CONSTRAINT ck_badges_code
        CHECK (code IN ('FIRST_QUESTION', 'FIRST_ACCEPTED_ANSWER', 'ANSWER_HELPER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_badges (
    user_badge_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    badge_id BIGINT NOT NULL,
    earned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    CONSTRAINT fk_user_badges_user
        FOREIGN KEY (user_id)
        REFERENCES users (user_id),
    CONSTRAINT fk_user_badges_badge
        FOREIGN KEY (badge_id)
        REFERENCES badges (badge_id),
    CONSTRAINT uk_user_badges_user_badge UNIQUE (user_id, badge_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE esg_metric_snapshots (
    esg_metric_snapshot_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_type VARCHAR(30) NOT NULL,
    target_id BIGINT NULL,
    knowledge_share_count BIGINT NOT NULL DEFAULT 0,
    accepted_answer_count BIGINT NOT NULL DEFAULT 0,
    estimated_saved_minutes BIGINT NOT NULL DEFAULT 0,
    source_backed_answer_rate DECIMAL(5,4) NOT NULL DEFAULT 0,
    ticket_completion_rate DECIMAL(5,4) NOT NULL DEFAULT 0,
    measured_date DATE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    CONSTRAINT ck_esg_metric_snapshots_target_type
        CHECK (target_type IN ('USER', 'TEAM', 'SYSTEM')),
    CONSTRAINT uk_esg_metric_snapshots_target_date UNIQUE (target_type, target_id, measured_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_user_badges_user_id ON user_badges (user_id);
CREATE INDEX idx_esg_metric_snapshots_measured_date ON esg_metric_snapshots (measured_date);
