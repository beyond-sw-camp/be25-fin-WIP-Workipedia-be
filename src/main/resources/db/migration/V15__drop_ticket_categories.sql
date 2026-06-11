ALTER TABLE tickets
    DROP FOREIGN KEY fk_tickets_category;

ALTER TABLE tickets
    DROP FOREIGN KEY fk_tickets_question;

DROP INDEX idx_tickets_category_id ON tickets;

ALTER TABLE tickets
    DROP COLUMN category_id,
    DROP COLUMN question_id;

DROP TABLE IF EXISTS department_category_mappings;

DROP TABLE IF EXISTS categories;

DROP TABLE IF EXISTS knowledge_candidates;

CREATE TABLE knowledge_data (
    knowledge_data_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content LONGTEXT NOT NULL,
    department_id BIGINT NULL,
    approved_by BIGINT NOT NULL,
    approved_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
    modified_source VARCHAR(30) NULL,
    CONSTRAINT fk_knowledge_data_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES tickets (ticket_id),
    CONSTRAINT fk_knowledge_data_department
        FOREIGN KEY (department_id)
        REFERENCES departments (department_id),
    CONSTRAINT fk_knowledge_data_approved_by
        FOREIGN KEY (approved_by)
        REFERENCES users (user_id),
    CONSTRAINT uk_knowledge_data_ticket UNIQUE (ticket_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_knowledge_data_department_id ON knowledge_data (department_id);
CREATE INDEX idx_knowledge_data_approved_at ON knowledge_data (approved_at);
