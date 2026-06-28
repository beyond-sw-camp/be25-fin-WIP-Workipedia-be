package com.wip.workipedia.notification.controller;

import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.notification.domain.NotificationTab;
import com.wip.workipedia.notification.dto.NotificationResponse;
import com.wip.workipedia.notification.dto.UnreadCountResponse;
import com.wip.workipedia.notification.service.NotificationService;
import com.wip.workipedia.notification.service.NotificationSseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationSseService notificationSseService;

    // 실시간 알림 SSE 구독. FE EventSource가 연결하며, 인증은 JwtFilter가 ?token= 쿼리로 처리한다.
    // 명시적 GET /stream 매핑이라 DELETE /{notificationId} 패턴과 충돌하지 않는다(405 해소).
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@AuthenticationPrincipal Long actorUserId) {
        return notificationSseService.subscribe(actorUserId);
    }

    // 전체 알림 목록 조회
    // 프론트는 응답의 targetUrl을 알림 클릭 시 이동 경로로 사용한다.
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

    // 개별 알림 읽음
    // 페이지 이동은 이 API 응답이 아니라 목록 조회에서 받은 targetUrl로 프론트가 처리한다.
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> read(
            @AuthenticationPrincipal Long actorUserId,
            @PathVariable Long notificationId) {
        notificationService.markAsRead(actorUserId, notificationId);
        return ResponseEntity.noContent().build();
    }

    // 전체 알림 읽음
    // 모든 알림의 readAt만 갱신하며 특정 페이지로 이동하지 않는다.
    @PatchMapping("/read-all")
    public ResponseEntity<Void> readAll(
            @AuthenticationPrincipal Long actorUserId) {
        notificationService.markAllAsRead(actorUserId);
        return ResponseEntity.noContent().build();
    }

    // 개별 알림 삭제
    // 물리 삭제가 아니라 deletedAt을 채워 알림 목록에서 제외한다.
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long actorUserId,
            @PathVariable Long notificationId) {
        notificationService.delete(actorUserId, notificationId);
        return ResponseEntity.noContent().build();
    }
}
