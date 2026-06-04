package com.wip.workipedia.notification.controller;

import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.notification.dto.NotificationResponse;
import com.wip.workipedia.notification.dto.UnreadCountResponse;
import com.wip.workipedia.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // TODO: 이슬이 시큐리티 통합 후 @AuthenticationPrincipal로 교체. 통합 전까지 X-User-Id 헤더로 대체.
    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponse>> list(
            @RequestHeader("X-User-Id") Long actorUserId,
            Pageable pageable) {
        return ResponseEntity.ok(notificationService.list(actorUserId, pageable));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> unreadCount(
            @RequestHeader("X-User-Id") Long actorUserId) {
        return ResponseEntity.ok(notificationService.unreadCount(actorUserId));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> read(
            @RequestHeader("X-User-Id") Long actorUserId,
            @PathVariable Long notificationId) {
        notificationService.markAsRead(actorUserId, notificationId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> readAll(
            @RequestHeader("X-User-Id") Long actorUserId) {
        notificationService.markAllAsRead(actorUserId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> delete(
            @RequestHeader("X-User-Id") Long actorUserId,
            @PathVariable Long notificationId) {
        notificationService.delete(actorUserId, notificationId);
        return ResponseEntity.noContent().build();
    }
}
