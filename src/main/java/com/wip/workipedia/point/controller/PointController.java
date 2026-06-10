package com.wip.workipedia.point.controller;

import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.point.dto.MyPointResponse;
import com.wip.workipedia.point.dto.PointHistoryResponse;
import com.wip.workipedia.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class PointController {
	private final PointService pointService;

	@GetMapping("/points")
	public ResponseEntity<MyPointResponse> getMyPoint(@AuthenticationPrincipal Long userId) {
		return ResponseEntity.ok(pointService.getMyPoint(userId));
	}

	@GetMapping("/point-histories")
	public ResponseEntity<PageResponse<PointHistoryResponse>> getMyPointHistory(
			@AuthenticationPrincipal Long userId,
			Pageable pageable) {
		return ResponseEntity.ok(pointService.getMyPointHistory(userId, pageable));
	}
}
