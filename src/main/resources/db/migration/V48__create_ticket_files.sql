CREATE TABLE ticket_files (
    ticket_file_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    file_key VARCHAR(500) NOT NULL,
    file_url VARCHAR(1000) NOT NULL,
    file_name VARCHAR(255) NULL,
    file_content_type VARCHAR(100) NULL,
    file_size BIGINT NULL,
    sort_order INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
    CONSTRAINT fk_ticket_files_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES tickets (ticket_id)
);

CREATE INDEX idx_ticket_files_ticket_id_sort_order
    ON ticket_files (ticket_id, sort_order);
