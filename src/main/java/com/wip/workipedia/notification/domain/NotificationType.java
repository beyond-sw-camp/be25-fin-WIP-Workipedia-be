package com.wip.workipedia.notification.domain;

// notifications.type CHECK 제약과 1:1 (V1__create_initial_schema.sql)
public enum NotificationType {
    WORKI_ANSWER_CREATED,
    WORKI_ANSWER_ACCEPTED,
    TICKET_STATUS_CHANGED,
    TICKET_TRANSFER_REQUESTED,
    COMMON_QUEUE_ASSIGNED,
    POINT_EARNED
}
