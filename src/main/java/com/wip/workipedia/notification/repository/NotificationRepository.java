package com.wip.workipedia.notification.repository;

import com.wip.workipedia.notification.domain.Notification;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId, Pageable pageable);

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
