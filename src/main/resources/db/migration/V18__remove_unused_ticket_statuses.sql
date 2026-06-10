UPDATE tickets
SET status = 'ASSIGNED'
WHERE status = 'IN_PROGRESS';

UPDATE tickets
SET status = 'COMMON_QUEUE'
WHERE status = 'REJECTED';

UPDATE ticket_status_logs
SET previous_status = 'ASSIGNED'
WHERE previous_status = 'IN_PROGRESS';

UPDATE ticket_status_logs
SET previous_status = 'COMMON_QUEUE'
WHERE previous_status = 'REJECTED';

UPDATE ticket_status_logs
SET new_status = 'ASSIGNED'
WHERE new_status = 'IN_PROGRESS';

UPDATE ticket_status_logs
SET new_status = 'COMMON_QUEUE'
WHERE new_status = 'REJECTED';

ALTER TABLE tickets
    DROP CONSTRAINT ck_tickets_status;

ALTER TABLE tickets
    ADD CONSTRAINT ck_tickets_status
        CHECK (status IN ('RECEIVED', 'COMMON_QUEUE', 'ASSIGNED', 'COMPLETED', 'DELETED'));

ALTER TABLE ticket_status_logs
    DROP CONSTRAINT ck_ticket_status_logs_previous_status;

ALTER TABLE ticket_status_logs
    ADD CONSTRAINT ck_ticket_status_logs_previous_status
        CHECK (previous_status IS NULL OR previous_status IN ('RECEIVED', 'COMMON_QUEUE', 'ASSIGNED', 'COMPLETED', 'DELETED'));

ALTER TABLE ticket_status_logs
    DROP CONSTRAINT ck_ticket_status_logs_new_status;

ALTER TABLE ticket_status_logs
    ADD CONSTRAINT ck_ticket_status_logs_new_status
        CHECK (new_status IN ('RECEIVED', 'COMMON_QUEUE', 'ASSIGNED', 'COMPLETED', 'DELETED'));
