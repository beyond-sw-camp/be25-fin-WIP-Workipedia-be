CREATE TABLE point_history (
    point_history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    point_amount INT NOT NULL,
    reason_type VARCHAR(50) NOT NULL,
    related_type VARCHAR(50) NULL,
    related_id BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_point_history_user
        FOREIGN KEY (user_id)
        REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_points (
    user_id BIGINT PRIMARY KEY,
    current_point BIGINT NOT NULL DEFAULT 0,
    esg_score BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    CONSTRAINT fk_user_points_user
        FOREIGN KEY (user_id)
        REFERENCES users (user_id),
    CONSTRAINT ck_user_points_current_point
        CHECK (current_point >= 0),
    CONSTRAINT ck_user_points_esg_score
        CHECK (esg_score >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notifications (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    target_type VARCHAR(50) NULL,
    target_id BIGINT NULL,
    target_url VARCHAR(500) NULL,
    read_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id)
        REFERENCES users (user_id),
    CONSTRAINT ck_notifications_type
        CHECK (type IN (
            'WORKI_ANSWER_CREATED',
            'WORKI_ANSWER_ACCEPTED',
            'TICKET_STATUS_CHANGED',
            'TICKET_TRANSFER_REQUESTED',
            'COMMON_QUEUE_ASSIGNED',
            'POINT_EARNED',
            'BADGE_EARNED'
        ))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_point_history_user_id ON point_history (user_id);
CREATE INDEX idx_point_history_reason_type ON point_history (reason_type);
CREATE INDEX idx_user_points_current_point ON user_points (current_point);
CREATE INDEX idx_user_points_esg_score ON user_points (esg_score);
CREATE INDEX idx_notifications_user_id ON notifications (user_id);
CREATE INDEX idx_notifications_type ON notifications (type);
CREATE INDEX idx_notifications_read_at ON notifications (read_at);
