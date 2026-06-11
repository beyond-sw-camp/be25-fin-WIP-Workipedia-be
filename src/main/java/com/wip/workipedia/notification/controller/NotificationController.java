package com.wip.workipedia.notification.controller;

import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.notification.domain.NotificationTab;
import com.wip.workipedia.notification.dto.NotificationResponse;
import com.wip.workipedia.notification.dto.UnreadCountResponse;
import com.wip.workipedia.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponse>> list(
            @AuthenticationPrincipal Long actorUserId,
            @RequestParam(defaultValue = "ALL") NotificationTab tab,
            Pageable pageable) {
        return ResponseEntity.ok(notificationService.list(actorUserId, tab, pageable));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> unreadCount(
            @AuthenticationPrincipal Long actorUserId) {
        return ResponseEntity.ok(notificationService.unreadCount(actorUserId));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> read(
            @AuthenticationPrincipal Long actorUserId,
            @PathVariable Long notificationId) {
        notificationService.markAsRead(actorUserId, notificationId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> readAll(
            @AuthenticationPrincipal Long actorUserId) {
        notificationService.markAllAsRead(actorUserId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long actorUserId,
            @PathVariable Long notificationId) {
        notificationService.delete(actorUserId, notificationId);
        return ResponseEntity.noContent().build();
    }
}
