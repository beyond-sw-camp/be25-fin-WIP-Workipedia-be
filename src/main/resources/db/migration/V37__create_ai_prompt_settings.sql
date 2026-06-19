CREATE TABLE ai_prompt_settings (
    ai_prompt_setting_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    custom_prompt        TEXT     NULL,
    is_active            CHAR(1)  NOT NULL DEFAULT 'N',
    created_at           DATETIME NOT NULL,
    updated_at           DATETIME NOT NULL,
    CONSTRAINT ck_ai_prompt_settings_is_active CHECK (is_active IN ('Y', 'N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO ai_prompt_settings (custom_prompt, is_active, created_at, updated_at)
VALUES (NULL, 'N', NOW(), NOW());
