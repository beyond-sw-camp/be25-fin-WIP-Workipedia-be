CREATE TABLE manual_files (
    manual_file_id BIGINT NOT NULL AUTO_INCREMENT,
    manual_id BIGINT NOT NULL,
    file_key VARCHAR(500) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    sort_order INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME NULL,
    PRIMARY KEY (manual_file_id),
    CONSTRAINT fk_manual_files_manual_id FOREIGN KEY (manual_id) REFERENCES manuals (manual_id)
);

CREATE INDEX idx_manual_files_manual_id_deleted_at_sort_order
    ON manual_files (manual_id, deleted_at, sort_order);

INSERT INTO manual_files (
    manual_id, file_key, file_url, sort_order,
    created_at, updated_at, deleted_at
)
SELECT
    manual_id, file_key, file_url, 1,
    COALESCE(created_at, NOW()), COALESCE(updated_at, NOW()), deleted_at
FROM manuals
WHERE file_key IS NOT NULL
  AND file_key <> ''
  AND file_url IS NOT NULL
  AND file_url <> '';
