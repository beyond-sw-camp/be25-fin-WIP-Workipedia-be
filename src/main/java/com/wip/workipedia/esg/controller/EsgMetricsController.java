package com.wip.workipedia.esg.controller;

import com.wip.workipedia.common.response.ApiResponse;
import com.wip.workipedia.esg.dto.EsgMetricsResponse;
import com.wip.workipedia.esg.service.EsgMetricsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EsgMetricsController {

	private final EsgMetricsService esgMetricsService;

	public EsgMetricsController(EsgMetricsService esgMetricsService) {
		this.esgMetricsService = esgMetricsService;
	}

	@GetMapping("/api/v1/esg/metrics/me")
	public ResponseEntity<ApiResponse<EsgMetricsResponse>> getMyMetrics() {
		return ApiResponse.success(HttpStatus.OK, "내 ESG 지표 조회 성공", esgMetricsService.getMyMetrics());
	}

	@GetMapping("/api/v1/admin/esg/metrics")
	public ResponseEntity<ApiResponse<EsgMetricsResponse>> getAdminMetrics() {
		return ApiResponse.success(HttpStatus.OK, "관리자 ESG 지표 조회 성공", esgMetricsService.getAdminMetrics());
	}
}
