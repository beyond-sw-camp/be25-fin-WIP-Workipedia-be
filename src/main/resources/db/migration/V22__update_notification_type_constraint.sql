UPDATE notifications
SET type = 'WORKI_QUESTION_ANSWERED'
WHERE type = 'WORKI_ANSWER_CREATED';

UPDATE notifications
SET type = 'TICKET_ASSIGNED'
WHERE type IN (
    'TICKET_STATUS_CHANGED',
    'TICKET_TRANSFER_REQUESTED',
    'COMMON_QUEUE_ASSIGNED'
);

UPDATE notifications
SET type = CASE
    WHEN target_type = 'WORKI_ANSWER'
         AND (title LIKE '%채택%' OR message LIKE '%채택%')
        THEN 'WORKI_ANSWER_ACCEPTED'
    WHEN target_type = 'WORKI_ANSWER'
        THEN 'WORKI_QUESTION_ANSWERED'
    WHEN target_type = 'WORKI_QUESTION'
        THEN 'WORKI_QUESTION_CREATED'
    ELSE 'WORKI_QUESTION_CREATED'
END
WHERE type = 'POINT_EARNED';

SET @drop_notification_target_type_constraint = (
    SELECT IF(
        COUNT(*) > 0,
        'ALTER TABLE notifications DROP CONSTRAINT ck_notifications_target_type',
        'SELECT 1'
    )
    FROM information_schema.table_constraints
    WHERE table_schema = DATABASE()
      AND table_name = 'notifications'
      AND constraint_name = 'ck_notifications_target_type'
);

PREPARE drop_notification_target_type_constraint_statement
    FROM @drop_notification_target_type_constraint;
EXECUTE drop_notification_target_type_constraint_statement;
DEALLOCATE PREPARE drop_notification_target_type_constraint_statement;

UPDATE notifications
SET target_type = 'MANUAL'
WHERE target_type = 'KNOWLEDGE_DATA';

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
            'MANUAL_UPDATED'
        ));

ALTER TABLE notifications
    ADD CONSTRAINT ck_notifications_target_type
        CHECK (
            target_type IS NULL
            OR target_type IN (
                'TICKET',
                'WORKI_QUESTION',
                'WORKI_ANSWER',
                'MANUAL'
            )
        );
