-- 워키 게시판: 질문 / 답변 / 반응
-- 선행: V1__create_departments_and_users.sql (users 테이블, user_id BIGINT PK)

CREATE TABLE worki_questions (
    question_id               BIGINT       NOT NULL AUTO_INCREMENT,
    user_id                   BIGINT       NOT NULL,
    title                     VARCHAR(255) NOT NULL,
    content                   TEXT         NOT NULL,
    status                    VARCHAR(20)  NOT NULL DEFAULT 'WAITING',
    view_count                BIGINT       NOT NULL DEFAULT 0,
    like_count                BIGINT       NOT NULL DEFAULT 0,
    source_chatbot_message_id BIGINT       NULL,
    created_at                DATETIME     NOT NULL,
    updated_at                DATETIME     NOT NULL,
    deleted_at                DATETIME     NULL,
    PRIMARY KEY (question_id),
    CONSTRAINT chk_worki_questions_status
        CHECK (status IN ('WAITING', 'IN_PROGRESS', 'ANSWERED', 'TICKETED', 'DELETED')),
    CONSTRAINT fk_worki_questions_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_worki_questions_user ON worki_questions (user_id);
CREATE INDEX idx_worki_questions_status ON worki_questions (status);

CREATE TABLE worki_answers (
    answer_id   BIGINT   NOT NULL AUTO_INCREMENT,
    question_id BIGINT   NOT NULL,
    user_id     BIGINT   NOT NULL,
    content     TEXT     NOT NULL,
    is_accepted BOOLEAN  NOT NULL DEFAULT FALSE,
    accepted_at DATETIME NULL,
    created_at  DATETIME NOT NULL,
    updated_at  DATETIME NOT NULL,
    deleted_at  DATETIME NULL,
    PRIMARY KEY (answer_id),
    CONSTRAINT fk_worki_answers_question
        FOREIGN KEY (question_id) REFERENCES worki_questions (question_id),
    CONSTRAINT fk_worki_answers_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_worki_answers_question ON worki_answers (question_id);

-- 워키 질문/답변 공용 반응 테이블 (기능 코드는 후속 단계, 스키마는 V2에서 함께 생성)
CREATE TABLE reactions (
    reaction_id   BIGINT      NOT NULL AUTO_INCREMENT,
    user_id       BIGINT      NOT NULL,
    target_type   VARCHAR(20) NOT NULL,
    target_id     BIGINT      NOT NULL,
    reaction_type VARCHAR(20) NOT NULL,
    created_at    DATETIME    NOT NULL,
    updated_at    DATETIME    NOT NULL,
    PRIMARY KEY (reaction_id),
    CONSTRAINT chk_reactions_target_type CHECK (target_type IN ('QUESTION', 'ANSWER')),
    CONSTRAINT chk_reactions_reaction_type CHECK (reaction_type IN ('LIKE', 'DISLIKE')),
    CONSTRAINT uq_reactions_user_target UNIQUE (user_id, target_type, target_id),
    CONSTRAINT fk_reactions_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
