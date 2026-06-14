package com.wip.workipedia.notification.repository;

import com.wip.workipedia.manual.domain.ManualStatus;
import com.wip.workipedia.notification.domain.Notification;
import com.wip.workipedia.notification.domain.NotificationTargetType;
import com.wip.workipedia.notification.domain.NotificationType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Notification> findByUserIdAndDeletedAtIsNullAndTargetTypeAndTypeInOrderByCreatedAtDesc(
            Long userId,
            NotificationTargetType targetType,
            List<NotificationType> types,
            Pageable pageable
    );

    Page<Notification> findByUserIdAndDeletedAtIsNullAndTargetTypeInAndTypeInOrderByCreatedAtDesc(
            Long userId,
            List<NotificationTargetType> targetTypes,
            List<NotificationType> types,
            Pageable pageable
    );

    default Page<Notification> findTicketTabNotifications(Long userId, Pageable pageable) {
        return findByUserIdAndDeletedAtIsNullAndTargetTypeAndTypeInOrderByCreatedAtDesc(
                userId,
                NotificationTargetType.TICKET,
                List.of(
                        NotificationType.TICKET_ASSIGNED,
                        NotificationType.TICKET_REASSIGNED,
                        NotificationType.TICKET_COMPLETED,
                        NotificationType.TICKET_DELETED
                ),
                pageable
        );
    }

    default Page<Notification> findWorkiTabNotifications(Long userId, Pageable pageable) {
        return findByUserIdAndDeletedAtIsNullAndTargetTypeInAndTypeInOrderByCreatedAtDesc(
                userId,
                List.of(NotificationTargetType.WORKI_QUESTION, NotificationTargetType.WORKI_ANSWER),
                List.of(
                        NotificationType.WORKI_QUESTION_CREATED,
                        NotificationType.WORKI_QUESTION_ANSWERED,
                        NotificationType.WORKI_ANSWER_ACCEPTED
                ),
                pageable
        );
    }

    default Page<Notification> findManualTabNotifications(Long userId, Pageable pageable) {
        return findManualTabNotifications(
                userId,
                NotificationType.MANUAL_UPDATED,
                NotificationTargetType.MANUAL,
                ManualStatus.PUBLISHED,
                NotificationType.DIRECT_DATA_ACTIVATED,
                NotificationTargetType.DIRECT_DATA,
                pageable
        );
    }

    @Query("""
            SELECT n
              FROM Notification n
             WHERE n.userId = :userId
               AND n.deletedAt IS NULL
               AND (
                   (
                       n.type = :manualType
                       AND n.targetType = :manualTargetType
                       AND EXISTS (
                           SELECT m.manualId
                             FROM Manual m
                            WHERE m.manualId = n.targetId
                              AND m.status = :manualStatus
                       )
                   )
                   OR
                   (
                       n.type = :directDataType
                       AND n.targetType = :directDataTargetType
                       AND EXISTS (
                           SELECT d.directDataId
                             FROM DirectData d
                            WHERE d.directDataId = n.targetId
                              AND d.deletedAt IS NULL
                              AND d.isActive = 'Y'
                       )
                   )
               )
             ORDER BY n.createdAt DESC
            """)
    Page<Notification> findManualTabNotifications(
            @Param("userId") Long userId,
            @Param("manualType") NotificationType manualType,
            @Param("manualTargetType") NotificationTargetType manualTargetType,
            @Param("manualStatus") ManualStatus manualStatus,
            @Param("directDataType") NotificationType directDataType,
            @Param("directDataTargetType") NotificationTargetType directDataTargetType,
            Pageable pageable
    );

    long countByUserIdAndReadAtIsNullAndDeletedAtIsNull(Long userId);

    Optional<Notification> findByNotificationIdAndDeletedAtIsNull(Long notificationId);

    @Modifying
    @Query("""
            UPDATE Notification n
               SET n.readAt = CURRENT_TIMESTAMP
             WHERE n.userId = :userId
               AND n.readAt IS NULL
               AND n.deletedAt IS NULL
            """)
    int markAllRead(Long userId);
}
