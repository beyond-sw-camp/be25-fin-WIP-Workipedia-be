ALTER TABLE manual_versions
    MODIFY manual_num VARCHAR(50) NOT NULL,
    ADD COLUMN title VARCHAR(255) NULL AFTER update_reason,
    ADD COLUMN content LONGTEXT NULL AFTER title,
    ADD COLUMN status VARCHAR(30) NULL AFTER content,
    ADD COLUMN source_url VARCHAR(500) NULL AFTER status,
    ADD COLUMN version VARCHAR(50) NULL AFTER source_url;
