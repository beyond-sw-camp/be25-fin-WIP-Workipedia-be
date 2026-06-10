package com.wip.workipedia.notification.domain;

import com.wip.workipedia.common.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 50)
    private NotificationTargetType targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "target_url", length = 500)
    private String targetUrl;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    private Notification(Long userId, NotificationType type, String title, String message,
                         NotificationTargetType targetType, Long targetId, String targetUrl) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.targetType = targetType;
        this.targetId = targetId;
        this.targetUrl = targetUrl;
    }

    public static Notification create(Long userId, NotificationType type, String title, String message,
                                      NotificationTargetType targetType, Long targetId, String targetUrl) {
        return new Notification(userId, type, title, message, targetType, targetId, targetUrl);
    }

    // 이미 읽음 처리된 알림에 다시 호출돼도 timestamp가 갱신되지 않도록 멱등성 확보.
    public void markAsRead() {
        if (this.readAt == null) {
            this.readAt = LocalDateTime.now();
        }
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }

    public boolean isUnread() {
        return this.readAt == null;
    }
}
