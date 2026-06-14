ALTER TABLE tickets
    DROP CONSTRAINT ck_tickets_status;

ALTER TABLE tickets
    ADD CONSTRAINT ck_tickets_status
        CHECK (status IN ('COMMON_QUEUE', 'ASSIGNED', 'TRANSFERRED', 'COMPLETED', 'DELETED'));

ALTER TABLE ticket_status_logs
    DROP CONSTRAINT ck_ticket_status_logs_previous_status;

ALTER TABLE ticket_status_logs
    ADD CONSTRAINT ck_ticket_status_logs_previous_status
        CHECK (previous_status IS NULL OR previous_status IN ('COMMON_QUEUE', 'ASSIGNED', 'TRANSFERRED', 'COMPLETED', 'DELETED'));

ALTER TABLE ticket_status_logs
    DROP CONSTRAINT ck_ticket_status_logs_new_status;

ALTER TABLE ticket_status_logs
    ADD CONSTRAINT ck_ticket_status_logs_new_status
        CHECK (new_status IN ('COMMON_QUEUE', 'ASSIGNED', 'TRANSFERRED', 'COMPLETED', 'DELETED'));
