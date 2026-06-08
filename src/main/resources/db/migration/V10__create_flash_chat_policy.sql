-- flash_chat_policy: 운영 정책 단일 행 테이블
CREATE TABLE flash_chat_policy (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_ttl_seconds   INT NOT NULL DEFAULT 600,
    send_cooldown_seconds INT NOT NULL DEFAULT 0,
    banned_words          JSON NULL,
    created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at            DATETIME NULL,
    is_deleted            CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO flash_chat_policy (message_ttl_seconds, send_cooldown_seconds, banned_words)
VALUES (600, 0, NULL);

-- admin_logs action_type CHECK 제약에 Flash Chat 액션 타입 추가
ALTER TABLE admin_logs DROP CONSTRAINT ck_admin_logs_action_type;

ALTER TABLE admin_logs ADD CONSTRAINT ck_admin_logs_action_type
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
        'KNOWLEDGE_PUBLISH',
        'FLASH_CHAT_MESSAGE_DELETE',
        'FLASH_CHAT_CONFIG_UPDATE'
    ));
