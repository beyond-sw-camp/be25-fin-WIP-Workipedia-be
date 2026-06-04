package com.wip.workipedia.point.controller;

import com.wip.workipedia.point.dto.MyPointResponse;
import com.wip.workipedia.point.dto.PointHistoryResponse;
import com.wip.workipedia.point.service.PointService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
public class PointController {
	private final PointService pointService;

	@GetMapping("/me")
	public ResponseEntity<MyPointResponse> getMyPoint() {
		return ResponseEntity.ok(pointService.getMyPoint());
	}

	@GetMapping("/me/history")
	public ResponseEntity<List<PointHistoryResponse>> getMyPointHistory() {
		return ResponseEntity.ok(pointService.getMyPointHistory());
	}
}
