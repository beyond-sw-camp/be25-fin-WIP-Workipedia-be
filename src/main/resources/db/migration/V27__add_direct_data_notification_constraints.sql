ALTER TABLE notifications
    DROP CONSTRAINT ck_notifications_type;

ALTER TABLE notifications
    ADD CONSTRAINT ck_notifications_type
        CHECK (type IN (
            'TICKET_ASSIGNED',
            'TICKET_COMPLETED',
            'TICKET_DELETED',
            'WORKI_QUESTION_CREATED',
            'WORKI_QUESTION_ANSWERED',
            'WORKI_ANSWER_ACCEPTED',
            'MANUAL_UPDATED',
            'DIRECT_DATA_ACTIVATED'
        ));

ALTER TABLE notifications
    DROP CONSTRAINT ck_notifications_target_type;

ALTER TABLE notifications
    ADD CONSTRAINT ck_notifications_target_type
        CHECK (
            target_type IS NULL
            OR target_type IN (
                'TICKET',
                'WORKI_QUESTION',
                'WORKI_ANSWER',
                'MANUAL',
                'DIRECT_DATA'
            )
        );
