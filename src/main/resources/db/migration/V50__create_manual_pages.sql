CREATE TABLE manual_pages (
    manual_page_id BIGINT NOT NULL AUTO_INCREMENT,
    manual_id BIGINT NOT NULL,
    file_key VARCHAR(500) NOT NULL,
    file_name VARCHAR(500) NOT NULL,
    file_sort_order INT NOT NULL,
    page_number INT NOT NULL,
    global_page_number INT NOT NULL,
    content LONGTEXT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME NULL,
    PRIMARY KEY (manual_page_id),
    CONSTRAINT fk_manual_pages_manual_id FOREIGN KEY (manual_id) REFERENCES manuals (manual_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_manual_pages_manual_id_deleted_at
    ON manual_pages (manual_id, deleted_at, file_sort_order, page_number);
