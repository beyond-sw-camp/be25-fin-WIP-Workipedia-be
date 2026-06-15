ALTER TABLE notifications
    DROP CONSTRAINT ck_notifications_type;

ALTER TABLE notifications
    ADD CONSTRAINT ck_notifications_type
        CHECK (type IN (
            'TICKET_ASSIGNED',
            'TICKET_REASSIGNED',
            'TICKET_COMPLETED',
            'TICKET_DELETED',
            'WORKI_QUESTION_CREATED',
            'WORKI_QUESTION_ANSWERED',
            'WORKI_ANSWER_ACCEPTED',
            'MANUAL_UPDATED',
            'DIRECT_DATA_ACTIVATED'
        ));
