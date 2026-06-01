CREATE TABLE departments (
    department_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    description VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
    CONSTRAINT uk_departments_code UNIQUE (code),
    CONSTRAINT uk_departments_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    description VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
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
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
    CONSTRAINT fk_department_category_mappings_department
        FOREIGN KEY (department_id)
        REFERENCES departments (department_id),
    CONSTRAINT fk_department_category_mappings_category
        FOREIGN KEY (category_id)
        REFERENCES categories (category_id),
    CONSTRAINT uk_department_category_mappings_department_category
        UNIQUE (department_id, category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    department_id BIGINT NOT NULL,
    employee_id VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'USER',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
    CONSTRAINT uk_users_employee_id UNIQUE (employee_id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_nickname UNIQUE (nickname),
    CONSTRAINT fk_users_department
        FOREIGN KEY (department_id)
        REFERENCES departments (department_id),
    CONSTRAINT ck_users_role
        CHECK (role IN ('USER', 'TEAM_ADMIN', 'SYSTEM_ADMIN')),
    CONSTRAINT ck_users_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE chatbot_sessions (
    session_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
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
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
    CONSTRAINT fk_chatbot_messages_session
        FOREIGN KEY (session_id)
        REFERENCES chatbot_sessions (session_id),
    CONSTRAINT ck_chatbot_messages_sender_type
        CHECK (sender_type IN ('USER', 'ASSISTANT', 'SYSTEM')),
    CONSTRAINT ck_chatbot_messages_next_action
        CHECK (next_action IS NULL OR next_action IN ('SHOW_SOURCES', 'CREATE_WORKI', 'CREATE_TICKET'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE worki_questions (
    question_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id BIGINT NOT NULL,
    source_chatbot_message_id BIGINT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'WAITING',
    accepted_answer_id BIGINT NULL,
    view_count BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
    CONSTRAINT fk_worki_questions_author
        FOREIGN KEY (author_id)
        REFERENCES users (user_id),
    CONSTRAINT fk_worki_questions_source_chatbot_message
        FOREIGN KEY (source_chatbot_message_id)
        REFERENCES chatbot_messages (message_id),
    CONSTRAINT ck_worki_questions_status
        CHECK (status IN ('WAITING', 'IN_PROGRESS', 'ANSWERED', 'TICKETED', 'DELETED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE worki_answers (
    answer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    ticket_id BIGINT NULL,
    content TEXT NOT NULL,
    official BOOLEAN NOT NULL DEFAULT FALSE,
    accepted BOOLEAN NOT NULL DEFAULT FALSE,
    accepted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
    CONSTRAINT fk_worki_answers_question
        FOREIGN KEY (question_id)
        REFERENCES worki_questions (question_id),
    CONSTRAINT fk_worki_answers_author
        FOREIGN KEY (author_id)
        REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE worki_questions
    ADD CONSTRAINT fk_worki_questions_accepted_answer
        FOREIGN KEY (accepted_answer_id)
        REFERENCES worki_answers (answer_id);

ALTER TABLE chatbot_messages
    ADD CONSTRAINT fk_chatbot_messages_source_worki_question
        FOREIGN KEY (source_worki_question_id)
        REFERENCES worki_questions (question_id);

CREATE TABLE reactions (
    reaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_type VARCHAR(30) NOT NULL,
    target_id BIGINT NOT NULL,
    reaction_type VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
    CONSTRAINT fk_reactions_user
        FOREIGN KEY (user_id)
        REFERENCES users (user_id),
    CONSTRAINT ck_reactions_target_type
        CHECK (target_type IN ('WORKI_QUESTION', 'WORKI_ANSWER')),
    CONSTRAINT ck_reactions_reaction_type
        CHECK (reaction_type IN ('LIKE', 'DISLIKE')),
    CONSTRAINT uk_reactions_user_target UNIQUE (user_id, target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
    CONSTRAINT fk_tickets_requester
        FOREIGN KEY (requester_id)
        REFERENCES users (user_id),
    CONSTRAINT fk_tickets_question
        FOREIGN KEY (question_id)
        REFERENCES worki_questions (question_id),
    CONSTRAINT fk_tickets_source_chatbot_message
        FOREIGN KEY (source_chatbot_message_id)
        REFERENCES chatbot_messages (message_id),
    CONSTRAINT fk_tickets_category
        FOREIGN KEY (category_id)
        REFERENCES categories (category_id),
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
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
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
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
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
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
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
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
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
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
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
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
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

CREATE TABLE point_history (
    point_history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    point_amount INT NOT NULL,
    reason_type VARCHAR(50) NOT NULL,
    related_type VARCHAR(50) NULL,
    related_id BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
    CONSTRAINT fk_point_history_user
        FOREIGN KEY (user_id)
        REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE points_daily_limit (
    user_id BIGINT NOT NULL,
    point_date DATE NOT NULL,
    today_point BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
    PRIMARY KEY (user_id, point_date),
    CONSTRAINT fk_points_daily_limit_user
        FOREIGN KEY (user_id)
        REFERENCES users (user_id),
    CONSTRAINT ck_points_daily_limit_today_point
        CHECK (today_point >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE esg_grade (
    grade_id INT AUTO_INCREMENT PRIMARY KEY,
    grade_name VARCHAR(20) NOT NULL,
    min_score BIGINT NOT NULL,
    max_score BIGINT NULL,
    badge_image_url VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
    CONSTRAINT uk_esg_grade_grade_name UNIQUE (grade_name),
    CONSTRAINT ck_esg_grade_min_score
        CHECK (min_score >= 0),
    CONSTRAINT ck_esg_grade_max_score
        CHECK (max_score IS NULL OR max_score > min_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_points (
    user_id BIGINT PRIMARY KEY,
    grade_id INT NOT NULL,
    current_point BIGINT NOT NULL DEFAULT 0,
    esg_score BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
    CONSTRAINT fk_user_points_user
        FOREIGN KEY (user_id)
        REFERENCES users (user_id),
    CONSTRAINT fk_user_points_grade
        FOREIGN KEY (grade_id)
        REFERENCES esg_grade (grade_id),
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
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
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

CREATE TABLE admin_logs (
    admin_log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_id BIGINT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    target_type VARCHAR(50) NULL,
    target_id BIGINT NULL,
    description VARCHAR(1000) NULL,
    metadata_json JSON NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
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

CREATE TABLE manuals (
    manual_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    department_id BIGINT NULL,
    title VARCHAR(255) NOT NULL,
    content LONGTEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PUBLISHED',
    source_url VARCHAR(500) NULL,
    version VARCHAR(50) NULL,
    created_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
    CONSTRAINT fk_manuals_department
        FOREIGN KEY (department_id)
        REFERENCES departments (department_id),
    CONSTRAINT fk_manuals_created_by
        FOREIGN KEY (created_by)
        REFERENCES users (user_id),
    CONSTRAINT ck_manuals_status
        CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED', 'DELETED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE manual_versions (
    manual_version_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    manual_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    manual_num VARCHAR(20) NOT NULL,
    update_reason TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
    CONSTRAINT fk_manual_versions_manual
        FOREIGN KEY (manual_id)
        REFERENCES manuals (manual_id),
    CONSTRAINT fk_manual_versions_user
        FOREIGN KEY (user_id)
        REFERENCES users (user_id),
    CONSTRAINT uk_manual_versions_manual_num UNIQUE (manual_id, manual_num)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE manual_chunks (
    manual_chunk_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    manual_id BIGINT NOT NULL,
    chunk_index INT NOT NULL,
    content TEXT NOT NULL,
    embedding_json JSON NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
    CONSTRAINT fk_manual_chunks_manual
        FOREIGN KEY (manual_id)
        REFERENCES manuals (manual_id),
    CONSTRAINT uk_manual_chunks_manual_index UNIQUE (manual_id, chunk_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE manual_citations (
    citation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_type VARCHAR(30) NOT NULL,
    source_id BIGINT NOT NULL,
    manual_id BIGINT NOT NULL,
    manual_chunk_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
    CONSTRAINT fk_manual_citations_manual
        FOREIGN KEY (manual_id)
        REFERENCES manuals (manual_id),
    CONSTRAINT fk_manual_citations_manual_chunk
        FOREIGN KEY (manual_chunk_id)
        REFERENCES manual_chunks (manual_chunk_id),
    CONSTRAINT ck_manual_citations_source_type
        CHECK (source_type IN ('CHATBOT_MESSAGE', 'WORKI_ANSWER', 'TICKET_ANSWER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE worki_chunks (
    worki_chunk_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_type VARCHAR(30) NOT NULL,
    source_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_id BIGINT NULL,
    chunk_index INT NOT NULL,
    content TEXT NOT NULL,
    embedding_json JSON NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
    CONSTRAINT fk_worki_chunks_question
        FOREIGN KEY (question_id)
        REFERENCES worki_questions (question_id),
    CONSTRAINT fk_worki_chunks_answer
        FOREIGN KEY (answer_id)
        REFERENCES worki_answers (answer_id),
    CONSTRAINT ck_worki_chunks_source_type
        CHECK (source_type IN ('QUESTION', 'ANSWER')),
    CONSTRAINT uk_worki_chunks_source_index UNIQUE (source_type, source_id, chunk_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE worki_search_logs (
    search_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    query_text VARCHAR(500) NOT NULL,
    selected_target_type VARCHAR(30) NULL,
    selected_target_id BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
    CONSTRAINT fk_worki_search_logs_user
        FOREIGN KEY (user_id)
        REFERENCES users (user_id),
    CONSTRAINT ck_worki_search_logs_selected_target_type
        CHECK (selected_target_type IS NULL OR selected_target_type IN ('QUESTION', 'ANSWER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE badges (
    badge_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
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
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
    CONSTRAINT fk_user_badges_user
        FOREIGN KEY (user_id)
        REFERENCES users (user_id),
    CONSTRAINT fk_user_badges_badge
        FOREIGN KEY (badge_id)
        REFERENCES badges (badge_id),
    CONSTRAINT uk_user_badges_user_badge UNIQUE (user_id, badge_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_department_category_mappings_category_id ON department_category_mappings (category_id);
CREATE INDEX idx_users_department_id ON users (department_id);
CREATE INDEX idx_users_role ON users (role);
CREATE INDEX idx_users_status ON users (status);
CREATE INDEX idx_chatbot_sessions_user_id ON chatbot_sessions (user_id);
CREATE INDEX idx_chatbot_messages_session_id ON chatbot_messages (session_id);
CREATE INDEX idx_chatbot_messages_created_at ON chatbot_messages (created_at);
CREATE INDEX idx_worki_questions_author_id ON worki_questions (author_id);
CREATE INDEX idx_worki_questions_status ON worki_questions (status);
CREATE INDEX idx_worki_answers_question_id ON worki_answers (question_id);
CREATE INDEX idx_worki_answers_author_id ON worki_answers (author_id);
CREATE INDEX idx_reactions_target ON reactions (target_type, target_id);
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
CREATE INDEX idx_point_history_user_id ON point_history (user_id);
CREATE INDEX idx_point_history_reason_type ON point_history (reason_type);
CREATE INDEX idx_points_daily_limit_point_date ON points_daily_limit (point_date);
CREATE INDEX idx_user_points_grade_id ON user_points (grade_id);
CREATE INDEX idx_user_points_current_point ON user_points (current_point);
CREATE INDEX idx_user_points_esg_score ON user_points (esg_score);
CREATE INDEX idx_notifications_user_id ON notifications (user_id);
CREATE INDEX idx_notifications_type ON notifications (type);
CREATE INDEX idx_notifications_read_at ON notifications (read_at);
CREATE INDEX idx_admin_logs_actor_id ON admin_logs (actor_id);
CREATE INDEX idx_admin_logs_action_type ON admin_logs (action_type);
CREATE INDEX idx_admin_logs_target ON admin_logs (target_type, target_id);
CREATE INDEX idx_manuals_department_id ON manuals (department_id);
CREATE INDEX idx_manuals_status ON manuals (status);
CREATE INDEX idx_manual_versions_manual_id ON manual_versions (manual_id);
CREATE INDEX idx_manual_chunks_manual_id ON manual_chunks (manual_id);
CREATE INDEX idx_manual_citations_source ON manual_citations (source_type, source_id);
CREATE INDEX idx_manual_citations_manual_chunk_id ON manual_citations (manual_chunk_id);
CREATE INDEX idx_worki_chunks_source ON worki_chunks (source_type, source_id);
CREATE INDEX idx_worki_chunks_question_id ON worki_chunks (question_id);
CREATE INDEX idx_worki_chunks_answer_id ON worki_chunks (answer_id);
CREATE INDEX idx_worki_search_logs_user_id ON worki_search_logs (user_id);
CREATE INDEX idx_worki_search_logs_selected_target ON worki_search_logs (selected_target_type, selected_target_id);
CREATE INDEX idx_user_badges_user_id ON user_badges (user_id);
