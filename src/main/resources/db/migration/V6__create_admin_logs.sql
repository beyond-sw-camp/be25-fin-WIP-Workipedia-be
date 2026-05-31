CREATE TABLE admin_logs (
    admin_log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_id BIGINT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    target_type VARCHAR(50) NULL,
    target_id BIGINT NULL,
    description VARCHAR(1000) NULL,
    metadata_json JSON NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_admin_logs_actor
        FOREIGN KEY (actor_id)
        REFERENCES users (user_id),
    CONSTRAINT ck_admin_logs_action_type
        CHECK (action_type IN (
            'USER_DEACTIVATE',
            'WORKI_READ',
            'WORKI_UPDATE',
            'WORKI_DELETE',
            'MANUAL_UPDATE',
            'MANUAL_DELETE',
            'TICKET_ASSIGN',
            'TICKET_TRANSFER_REQUEST',
            'TICKET_ROUTE_OVERRIDE',
            'COMMON_QUEUE_ASSIGN',
            'KNOWLEDGE_REVIEW',
            'KNOWLEDGE_PUBLISH'
        ))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_admin_logs_actor_id ON admin_logs (actor_id);
CREATE INDEX idx_admin_logs_action_type ON admin_logs (action_type);
CREATE INDEX idx_admin_logs_target ON admin_logs (target_type, target_id);
