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
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    CONSTRAINT fk_manuals_department
        FOREIGN KEY (department_id)
        REFERENCES departments (department_id),
    CONSTRAINT fk_manuals_created_by
        FOREIGN KEY (created_by)
        REFERENCES users (user_id),
    CONSTRAINT ck_manuals_status
        CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED', 'DELETED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE manual_chunks (
    manual_chunk_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    manual_id BIGINT NOT NULL,
    chunk_index INT NOT NULL,
    content TEXT NOT NULL,
    embedding_json JSON NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    CONSTRAINT fk_manual_chunks_manual
        FOREIGN KEY (manual_id)
        REFERENCES manuals (manual_id),
    CONSTRAINT uk_manual_chunks_manual_index UNIQUE (manual_id, chunk_index)
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
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
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

CREATE INDEX idx_manuals_department_id ON manuals (department_id);
CREATE INDEX idx_manuals_status ON manuals (status);
CREATE INDEX idx_manual_chunks_manual_id ON manual_chunks (manual_id);
CREATE INDEX idx_worki_chunks_source ON worki_chunks (source_type, source_id);
CREATE INDEX idx_worki_chunks_question_id ON worki_chunks (question_id);
CREATE INDEX idx_worki_chunks_answer_id ON worki_chunks (answer_id);
