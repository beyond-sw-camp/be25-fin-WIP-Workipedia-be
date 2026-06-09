package com.wip.workipedia.mypage.controller;

import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.mypage.domain.MyTicketStatus;
import com.wip.workipedia.mypage.dto.MyPageResponse;
import com.wip.workipedia.mypage.dto.MyTicketDetailResponse;
import com.wip.workipedia.mypage.dto.MyTicketResponse;
import com.wip.workipedia.mypage.service.MyPageService;
import com.wip.workipedia.notification.dto.NotificationSettingRequest;
import com.wip.workipedia.notification.service.NotificationSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class MyPageController {

	private final MyPageService myPageService;
	private final NotificationSettingService notificationSettingService;

	// 마이페이지 조회
	@GetMapping("/profile")
	public ResponseEntity<MyPageResponse> getMyPage(
		@AuthenticationPrincipal Long userId
	) {
		MyPageResponse myPageResponse = myPageService.getMyPage(userId);

		return ResponseEntity.ok(myPageResponse);
	}

	// 알림 설정 변경
	@PatchMapping("/notification-settings")
	public ResponseEntity<MyPageResponse.NotificationSettings> updateNotificationSettings(
		@AuthenticationPrincipal Long userId,
		@Valid @RequestBody NotificationSettingRequest notificationSettingRequest
	) {
		MyPageResponse.NotificationSettings notificationSettings = notificationSettingService.update(
			userId,
			notificationSettingRequest
		);

		return ResponseEntity.ok(notificationSettings);
	}

	// 내 발행 티켓 목록 조회
	@GetMapping("/tickets")
	public ResponseEntity<PageResponse<MyTicketResponse>> getMyTickets(
			@AuthenticationPrincipal Long userId,
			@RequestParam(required = false) MyTicketStatus status,
			Pageable pageable
	) {
		PageResponse<MyTicketResponse> myTicketResponses = myPageService.getMyTickets(userId, status, pageable);

		return ResponseEntity.ok(myTicketResponses);
	}

	// 내 발행 티켓 상세 조회
	@GetMapping("/tickets/{ticketId}")
	public ResponseEntity<MyTicketDetailResponse> getMyTicketDetail(
		@AuthenticationPrincipal Long userId,
		@PathVariable Long ticketId
	) {
		MyTicketDetailResponse myTicketDetailResponse = myPageService.getMyTicketDetail(userId, ticketId);

		return ResponseEntity.ok(myTicketDetailResponse);
	}
}
