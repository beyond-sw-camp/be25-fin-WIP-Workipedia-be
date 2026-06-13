package com.wip.workipedia.notification.dto;

import com.wip.workipedia.notification.domain.Notification;
import com.wip.workipedia.notification.domain.NotificationTargetType;
import com.wip.workipedia.notification.domain.NotificationType;
import java.time.LocalDateTime;

public record NotificationResponse(
        Long notificationId,
        NotificationType type,
        String title,
        String message,
        NotificationTargetType targetType,
        Long targetId,
        // 프론트는 알림 클릭 시 이 경로로 라우팅한다.
        String targetUrl,
        Integer pointAmount,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getNotificationId(),
                n.getType(),
                n.getTitle(),
                n.getMessage(),
                n.getTargetType(),
                n.getTargetId(),
                n.getTargetUrl(),
                n.getPointAmount(),
                n.getReadAt(),
                n.getCreatedAt()
        );
    }
}
