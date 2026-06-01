package com.wip.workipedia.badge.controller;

import com.wip.workipedia.badge.dto.BadgeResponse;
import com.wip.workipedia.badge.service.BadgeService;
import com.wip.workipedia.common.response.ApiResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/badges")
public class BadgeController {

	private final BadgeService badgeService;

	public BadgeController(BadgeService badgeService) {
		this.badgeService = badgeService;
	}

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<List<BadgeResponse>>> getMyBadges() {
		return ApiResponse.success(HttpStatus.OK, "내 뱃지 목록 조회 성공", badgeService.getMyBadges());
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<BadgeResponse>>> getBadges() {
		return ApiResponse.success(HttpStatus.OK, "전체 뱃지 기준 조회 성공", badgeService.getBadges());
	}
}
