CREATE TABLE categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    description VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    CONSTRAINT uk_categories_code UNIQUE (code),
    CONSTRAINT uk_categories_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE department_category_mappings (
    mapping_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    department_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    priority INT NOT NULL DEFAULT 1,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    CONSTRAINT fk_department_category_mappings_department
        FOREIGN KEY (department_id)
        REFERENCES departments (department_id),
    CONSTRAINT fk_department_category_mappings_category
        FOREIGN KEY (category_id)
        REFERENCES categories (category_id),
    CONSTRAINT uk_department_category_mappings_department_category
        UNIQUE (department_id, category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ticket_status_logs (
    status_log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    changed_by BIGINT NULL,
    previous_status VARCHAR(30) NULL,
    new_status VARCHAR(30) NOT NULL,
    reason VARCHAR(1000) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ticket_status_logs_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES tickets (ticket_id),
    CONSTRAINT fk_ticket_status_logs_changed_by
        FOREIGN KEY (changed_by)
        REFERENCES users (user_id),
    CONSTRAINT ck_ticket_status_logs_previous_status
        CHECK (previous_status IS NULL OR previous_status IN ('RECEIVED', 'COMMON_QUEUE', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'REJECTED', 'DELETED')),
    CONSTRAINT ck_ticket_status_logs_new_status
        CHECK (new_status IN ('RECEIVED', 'COMMON_QUEUE', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'REJECTED', 'DELETED'))
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

ALTER TABLE tickets
    ADD CONSTRAINT fk_tickets_category
        FOREIGN KEY (category_id)
        REFERENCES categories (category_id);

ALTER TABLE worki_questions
    ADD CONSTRAINT fk_worki_questions_source_chatbot_message
        FOREIGN KEY (source_chatbot_message_id)
        REFERENCES chatbot_messages (message_id);

ALTER TABLE manuals
    ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'PUBLISHED' AFTER content,
    ADD CONSTRAINT ck_manuals_status
        CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED', 'DELETED'));

ALTER TABLE worki_chunks
    ADD COLUMN source_type VARCHAR(30) NULL AFTER worki_chunk_id,
    ADD COLUMN source_id BIGINT NULL AFTER source_type;

UPDATE worki_chunks
SET source_type = CASE
        WHEN answer_id IS NULL THEN 'QUESTION'
        ELSE 'ANSWER'
    END,
    source_id = CASE
        WHEN answer_id IS NULL THEN question_id
        ELSE answer_id
    END;

ALTER TABLE worki_chunks
    MODIFY source_type VARCHAR(30) NOT NULL,
    MODIFY source_id BIGINT NOT NULL,
    DROP INDEX uk_worki_chunks_question_answer_index,
    ADD CONSTRAINT ck_worki_chunks_source_type
        CHECK (source_type IN ('QUESTION', 'ANSWER')),
    ADD CONSTRAINT uk_worki_chunks_source_index
        UNIQUE (source_type, source_id, chunk_index);

CREATE INDEX idx_department_category_mappings_category_id ON department_category_mappings (category_id);
CREATE INDEX idx_ticket_status_logs_ticket_id ON ticket_status_logs (ticket_id);
CREATE INDEX idx_user_points_current_point ON user_points (current_point);
CREATE INDEX idx_user_points_esg_score ON user_points (esg_score);
CREATE INDEX idx_tickets_category_id ON tickets (category_id);
CREATE INDEX idx_manuals_status ON manuals (status);
CREATE INDEX idx_worki_chunks_source ON worki_chunks (source_type, source_id);
