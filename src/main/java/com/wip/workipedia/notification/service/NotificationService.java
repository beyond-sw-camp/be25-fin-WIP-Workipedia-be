package com.wip.workipedia.notification.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.notification.domain.Notification;
import com.wip.workipedia.notification.dto.NotificationResponse;
import com.wip.workipedia.notification.dto.UnreadCountResponse;
import com.wip.workipedia.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public PageResponse<NotificationResponse> list(Long userId, Pageable pageable) {
        return PageResponse.from(
                notificationRepository
                        .findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId, pageable)
                        .map(NotificationResponse::from));
    }

    public UnreadCountResponse unreadCount(Long userId) {
        return new UnreadCountResponse(
                notificationRepository.countByUserIdAndReadAtIsNullAndDeletedAtIsNull(userId));
    }

    @Transactional
    public void markAsRead(Long actorUserId, Long notificationId) {
        Notification notification = getOwnedNotification(actorUserId, notificationId);
        notification.markAsRead();
    }

    @Transactional
    public void markAllAsRead(Long actorUserId) {
        notificationRepository.markAllRead(actorUserId);
    }

    @Transactional
    public void delete(Long actorUserId, Long notificationId) {
        Notification notification = getOwnedNotification(actorUserId, notificationId);
        notification.markDeleted();
    }

    private Notification getOwnedNotification(Long actorUserId, Long notificationId) {
        Notification notification = notificationRepository
                .findByNotificationIdAndDeletedAtIsNull(notificationId)
                .orElseThrow(() -> new CustomException(
                        ErrorType.NOTIFICATION_NOT_FOUND, "알림을 찾을 수 없습니다. id=" + notificationId));
        if (!notification.isOwnedBy(actorUserId)) {
            throw new CustomException(ErrorType.NOTIFICATION_FORBIDDEN);
        }
        return notification;
    }
}
