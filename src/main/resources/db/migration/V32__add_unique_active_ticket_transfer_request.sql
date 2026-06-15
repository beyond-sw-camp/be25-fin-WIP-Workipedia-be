ALTER TABLE ticket_transfer_requests
    ADD COLUMN modified_source VARCHAR(30) NULL,
    ADD COLUMN active_requested_ticket_id BIGINT
        GENERATED ALWAYS AS (
            CASE
                WHEN status = 'REQUESTED' AND deleted_at IS NULL THEN ticket_id
                ELSE NULL
            END
        ) STORED;

CREATE UNIQUE INDEX uk_ticket_transfer_requests_active_requested_ticket
    ON ticket_transfer_requests (active_requested_ticket_id);
