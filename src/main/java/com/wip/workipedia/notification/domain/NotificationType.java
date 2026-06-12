package com.wip.workipedia.notification.domain;

// notifications.type CHECK 제약조건과 동일하게 유지해야 한다.
public enum NotificationType {
    TICKET_ASSIGNED,
    TICKET_COMPLETED,
    TICKET_DELETED,
    WORKI_QUESTION_CREATED,
    WORKI_QUESTION_ANSWERED,
    WORKI_ANSWER_ACCEPTED,
    MANUAL_UPDATED
}
