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
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    CONSTRAINT fk_worki_questions_author
        FOREIGN KEY (author_id)
        REFERENCES users (user_id),
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
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
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

CREATE TABLE reactions (
    reaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_type VARCHAR(30) NOT NULL,
    target_id BIGINT NOT NULL,
    reaction_type VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    CONSTRAINT fk_reactions_user
        FOREIGN KEY (user_id)
        REFERENCES users (user_id),
    CONSTRAINT ck_reactions_target_type
        CHECK (target_type IN ('WORKI_QUESTION', 'WORKI_ANSWER')),
    CONSTRAINT ck_reactions_reaction_type
        CHECK (reaction_type IN ('LIKE', 'DISLIKE')),
    CONSTRAINT uk_reactions_user_target UNIQUE (user_id, target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_worki_questions_author_id ON worki_questions (author_id);
CREATE INDEX idx_worki_questions_status ON worki_questions (status);
CREATE INDEX idx_worki_answers_question_id ON worki_answers (question_id);
CREATE INDEX idx_worki_answers_author_id ON worki_answers (author_id);
CREATE INDEX idx_reactions_target ON reactions (target_type, target_id);
