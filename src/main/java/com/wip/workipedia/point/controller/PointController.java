package com.wip.workipedia.point.controller;

import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.point.dto.MyPointResponse;
import com.wip.workipedia.point.dto.PointHistorySearchType;
import com.wip.workipedia.point.dto.PointHistoryResponse;
import com.wip.workipedia.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class PointController {
	private final PointService pointService;

	// 현재 보유 포인트 조회
	@GetMapping("/points")
	public ResponseEntity<MyPointResponse> getMyPoint(@AuthenticationPrincipal Long userId) {
		return ResponseEntity.ok(pointService.getMyPoint(userId));
	}

	// 포인트 변동 내역 조회
	@GetMapping("/point-histories")
	public ResponseEntity<PageResponse<PointHistoryResponse>> getMyPointHistory(
			@AuthenticationPrincipal Long userId,
			@RequestParam(defaultValue = "ALL") PointHistorySearchType type,
			Pageable pageable) {
		return ResponseEntity.ok(pointService.getMyPointHistory(userId, type, pageable));
	}
}
