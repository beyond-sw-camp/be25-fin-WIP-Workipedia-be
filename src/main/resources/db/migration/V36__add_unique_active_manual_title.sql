ALTER TABLE manuals
    ADD COLUMN active_title VARCHAR(255)
        GENERATED ALWAYS AS (
            CASE
                WHEN deleted_at IS NULL AND is_deleted = 'N' THEN title
                ELSE NULL
            END
        ) STORED;

CREATE UNIQUE INDEX uk_manuals_active_title
    ON manuals (active_title);
