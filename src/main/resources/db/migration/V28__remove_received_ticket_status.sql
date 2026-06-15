UPDATE tickets
SET status = 'COMMON_QUEUE'
WHERE status = 'RECEIVED';

UPDATE ticket_status_logs
SET previous_status = 'COMMON_QUEUE'
WHERE previous_status = 'RECEIVED';

UPDATE ticket_status_logs
SET new_status = 'COMMON_QUEUE'
WHERE new_status = 'RECEIVED';

ALTER TABLE tickets
    MODIFY status VARCHAR(30) NOT NULL DEFAULT 'COMMON_QUEUE';

ALTER TABLE tickets
    DROP CONSTRAINT ck_tickets_status;

ALTER TABLE tickets
    ADD CONSTRAINT ck_tickets_status
        CHECK (status IN ('COMMON_QUEUE', 'ASSIGNED', 'COMPLETED', 'DELETED'));

ALTER TABLE ticket_status_logs
    DROP CONSTRAINT ck_ticket_status_logs_previous_status;

ALTER TABLE ticket_status_logs
    ADD CONSTRAINT ck_ticket_status_logs_previous_status
        CHECK (previous_status IS NULL OR previous_status IN ('COMMON_QUEUE', 'ASSIGNED', 'COMPLETED', 'DELETED'));

ALTER TABLE ticket_status_logs
    DROP CONSTRAINT ck_ticket_status_logs_new_status;

ALTER TABLE ticket_status_logs
    ADD CONSTRAINT ck_ticket_status_logs_new_status
        CHECK (new_status IN ('COMMON_QUEUE', 'ASSIGNED', 'COMPLETED', 'DELETED'));


ALTER TABLE ticket_answers
    ADD COLUMN file_key VARCHAR(500) NULL AFTER content,
    ADD COLUMN file_url VARCHAR(1000) NULL AFTER file_key,
    ADD COLUMN file_name VARCHAR(255) NULL AFTER file_url,
    ADD COLUMN file_content_type VARCHAR(100) NULL AFTER file_name,
    ADD COLUMN file_size BIGINT NULL AFTER file_content_type;
