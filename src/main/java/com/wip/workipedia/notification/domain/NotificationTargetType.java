package com.wip.workipedia.notification.domain;

// 알림 클릭 시 이동할 대상 리소스 타입. target_url과 함께 클라이언트 라우팅에 사용.
public enum NotificationTargetType {
    TICKET,
    WORKI_QUESTION,
    WORKI_ANSWER,
    MANUAL
}
