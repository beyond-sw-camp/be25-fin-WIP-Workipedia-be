CREATE TABLE tickets (
    ticket_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requester_id BIGINT NOT NULL,
    question_id BIGINT NULL,
    source_chatbot_message_id BIGINT NULL,
    category_id BIGINT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'RECEIVED',
    assigned_department_id BIGINT NULL,
    assignee_id BIGINT NULL,
    routing_confidence_score DECIMAL(5,2) NULL,
    routing_decision VARCHAR(50) NULL,
    completed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    CONSTRAINT fk_tickets_requester
        FOREIGN KEY (requester_id)
        REFERENCES users (user_id),
    CONSTRAINT fk_tickets_question
        FOREIGN KEY (question_id)
        REFERENCES worki_questions (question_id),
    CONSTRAINT fk_tickets_category
        FOREIGN KEY (category_id)
        REFERENCES categories (category_id),
    CONSTRAINT fk_tickets_source_chatbot_message
        FOREIGN KEY (source_chatbot_message_id)
        REFERENCES chatbot_messages (message_id),
    CONSTRAINT fk_tickets_assigned_department
        FOREIGN KEY (assigned_department_id)
        REFERENCES departments (department_id),
    CONSTRAINT fk_tickets_assignee
        FOREIGN KEY (assignee_id)
        REFERENCES users (user_id),
    CONSTRAINT ck_tickets_status
        CHECK (status IN ('RECEIVED', 'COMMON_QUEUE', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'REJECTED', 'DELETED')),
    CONSTRAINT ck_tickets_routing_decision
        CHECK (routing_decision IS NULL OR routing_decision IN ('AUTO_ASSIGNED', 'ADMIN_REVIEW', 'COMMON_QUEUE', 'NEED_MORE_INFO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ticket_answers (
    ticket_answer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    CONSTRAINT fk_ticket_answers_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES tickets (ticket_id),
    CONSTRAINT fk_ticket_answers_author
        FOREIGN KEY (author_id)
        REFERENCES users (user_id)
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

CREATE TABLE ticket_transfer_requests (
    transfer_request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    requester_id BIGINT NOT NULL,
    from_department_id BIGINT NOT NULL,
    suggested_department_id BIGINT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'REQUESTED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    CONSTRAINT fk_ticket_transfer_requests_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES tickets (ticket_id),
    CONSTRAINT fk_ticket_transfer_requests_requester
        FOREIGN KEY (requester_id)
        REFERENCES users (user_id),
    CONSTRAINT fk_ticket_transfer_requests_from_department
        FOREIGN KEY (from_department_id)
        REFERENCES departments (department_id),
    CONSTRAINT fk_ticket_transfer_requests_suggested_department
        FOREIGN KEY (suggested_department_id)
        REFERENCES departments (department_id),
    CONSTRAINT ck_ticket_transfer_requests_status
        CHECK (status IN ('REQUESTED', 'ASSIGNED_FROM_QUEUE', 'REJECTED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ticket_assignments (
    assignment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    assignee_id BIGINT NOT NULL,
    assigned_by BIGINT NOT NULL,
    memo VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ticket_assignments_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES tickets (ticket_id),
    CONSTRAINT fk_ticket_assignments_assignee
        FOREIGN KEY (assignee_id)
        REFERENCES users (user_id),
    CONSTRAINT fk_ticket_assignments_assigned_by
        FOREIGN KEY (assigned_by)
        REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ticket_routing_logs (
    routing_log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    decision VARCHAR(50) NOT NULL,
    confidence_score DECIMAL(5,2) NULL,
    candidate_departments_json JSON NULL,
    reasons_json JSON NULL,
    routed_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ticket_routing_logs_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES tickets (ticket_id),
    CONSTRAINT fk_ticket_routing_logs_routed_by
        FOREIGN KEY (routed_by)
        REFERENCES users (user_id),
    CONSTRAINT ck_ticket_routing_logs_decision
        CHECK (decision IN ('AUTO_ASSIGNED', 'ADMIN_REVIEW', 'COMMON_QUEUE', 'NEED_MORE_INFO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE knowledge_candidates (
    candidate_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    reviewed_by BIGINT NULL,
    published_worki_question_id BIGINT NULL,
    draft_title VARCHAR(255) NOT NULL,
    draft_content TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    review_comment VARCHAR(1000) NULL,
    reviewed_at DATETIME NULL,
    published_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    CONSTRAINT fk_knowledge_candidates_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES tickets (ticket_id),
    CONSTRAINT fk_knowledge_candidates_created_by
        FOREIGN KEY (created_by)
        REFERENCES users (user_id),
    CONSTRAINT fk_knowledge_candidates_reviewed_by
        FOREIGN KEY (reviewed_by)
        REFERENCES users (user_id),
    CONSTRAINT fk_knowledge_candidates_published_worki_question
        FOREIGN KEY (published_worki_question_id)
        REFERENCES worki_questions (question_id),
    CONSTRAINT ck_knowledge_candidates_status
        CHECK (status IN ('DRAFT', 'REVIEW_REQUESTED', 'APPROVED', 'REJECTED', 'PUBLISHED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE worki_answers
    ADD CONSTRAINT fk_worki_answers_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES tickets (ticket_id);

ALTER TABLE chatbot_messages
    ADD CONSTRAINT fk_chatbot_messages_source_ticket
        FOREIGN KEY (source_ticket_id)
        REFERENCES tickets (ticket_id);

CREATE INDEX idx_tickets_requester_id ON tickets (requester_id);
CREATE INDEX idx_tickets_status ON tickets (status);
CREATE INDEX idx_tickets_category_id ON tickets (category_id);
CREATE INDEX idx_tickets_assigned_department_id ON tickets (assigned_department_id);
CREATE INDEX idx_tickets_assignee_id ON tickets (assignee_id);
CREATE INDEX idx_ticket_answers_ticket_id ON ticket_answers (ticket_id);
CREATE INDEX idx_ticket_status_logs_ticket_id ON ticket_status_logs (ticket_id);
CREATE INDEX idx_ticket_transfer_requests_ticket_id ON ticket_transfer_requests (ticket_id);
CREATE INDEX idx_ticket_routing_logs_ticket_id ON ticket_routing_logs (ticket_id);
CREATE INDEX idx_knowledge_candidates_ticket_id ON knowledge_candidates (ticket_id);
CREATE INDEX idx_knowledge_candidates_status ON knowledge_candidates (status);
