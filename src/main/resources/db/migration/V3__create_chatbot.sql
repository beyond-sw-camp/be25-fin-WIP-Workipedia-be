CREATE TABLE chatbot_sessions (
    session_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    CONSTRAINT fk_chatbot_sessions_user
        FOREIGN KEY (user_id)
        REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE chatbot_messages (
    message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    sender_type VARCHAR(30) NOT NULL,
    content TEXT NOT NULL,
    answerable BOOLEAN NULL,
    next_action VARCHAR(50) NULL,
    references_json JSON NULL,
    source_worki_question_id BIGINT NULL,
    source_ticket_id BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    CONSTRAINT fk_chatbot_messages_session
        FOREIGN KEY (session_id)
        REFERENCES chatbot_sessions (session_id),
    CONSTRAINT fk_chatbot_messages_source_worki_question
        FOREIGN KEY (source_worki_question_id)
        REFERENCES worki_questions (question_id),
    CONSTRAINT ck_chatbot_messages_sender_type
        CHECK (sender_type IN ('USER', 'ASSISTANT', 'SYSTEM')),
    CONSTRAINT ck_chatbot_messages_next_action
        CHECK (next_action IS NULL OR next_action IN ('SHOW_SOURCES', 'CREATE_WORKI', 'CREATE_TICKET'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_chatbot_sessions_user_id ON chatbot_sessions (user_id);
CREATE INDEX idx_chatbot_messages_session_id ON chatbot_messages (session_id);
CREATE INDEX idx_chatbot_messages_created_at ON chatbot_messages (created_at);
