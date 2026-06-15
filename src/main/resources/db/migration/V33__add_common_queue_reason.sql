ALTER TABLE tickets
    ADD COLUMN common_queue_reason VARCHAR(30) NULL,
    ADD COLUMN common_queue_entered_at DATETIME NULL;

UPDATE tickets
SET common_queue_reason = CASE
        WHEN status = 'TRANSFERRED' THEN 'TRANSFER_REQUESTED'
        WHEN status = 'COMMON_QUEUE' THEN 'ROUTING_FAILED'
        ELSE NULL
    END,
    common_queue_entered_at = CASE
        WHEN status IN ('COMMON_QUEUE', 'TRANSFERRED') THEN COALESCE(updated_at, created_at)
        ELSE NULL
    END,
    status = CASE
        WHEN status = 'TRANSFERRED' THEN 'COMMON_QUEUE'
        ELSE status
    END
WHERE deleted_at IS NULL
  AND is_deleted = 'N'
  AND status IN ('COMMON_QUEUE', 'TRANSFERRED');

ALTER TABLE tickets
    ADD CONSTRAINT ck_tickets_common_queue_reason
        CHECK (common_queue_reason IS NULL OR common_queue_reason IN ('ROUTING_FAILED', 'TRANSFER_REQUESTED', 'ASSIGNMENT_EXPIRED'));

ALTER TABLE tickets
    DROP CONSTRAINT ck_tickets_status;

ALTER TABLE tickets
    ADD CONSTRAINT ck_tickets_status
        CHECK (status IN ('COMMON_QUEUE', 'ASSIGNED', 'COMPLETED', 'DELETED'));

UPDATE ticket_status_logs
SET previous_status = 'COMMON_QUEUE'
WHERE previous_status = 'TRANSFERRED';

UPDATE ticket_status_logs
SET new_status = 'COMMON_QUEUE'
WHERE new_status = 'TRANSFERRED';

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
