package com.wip.workipedia.point.controller;

import com.wip.workipedia.common.response.ApiResponse;
import com.wip.workipedia.point.dto.MyPointResponse;
import com.wip.workipedia.point.dto.PointHistoryResponse;
import com.wip.workipedia.point.dto.PointRankingResponse;
import com.wip.workipedia.point.service.PointService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/points")
public class PointController {

	private final PointService pointService;

	public PointController(PointService pointService) {
		this.pointService = pointService;
	}

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<MyPointResponse>> getMyPoint() {
		return ApiResponse.success(HttpStatus.OK, "내 포인트 조회 성공", pointService.getMyPoint());
	}

	@GetMapping("/me/history")
	public ResponseEntity<ApiResponse<List<PointHistoryResponse>>> getMyPointHistory() {
		return ApiResponse.success(HttpStatus.OK, "내 포인트 이력 조회 성공", pointService.getMyPointHistory());
	}

	@GetMapping("/ranking")
	public ResponseEntity<ApiResponse<List<PointRankingResponse>>> getRanking() {
		return ApiResponse.success(HttpStatus.OK, "포인트 랭킹 조회 성공", pointService.getRanking());
	}
}
