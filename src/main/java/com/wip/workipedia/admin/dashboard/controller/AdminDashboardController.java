package com.wip.workipedia.admin.dashboard.controller;

import com.wip.workipedia.admin.dashboard.dto.DepartmentAutoAssignmentRateResponse;
import com.wip.workipedia.admin.dashboard.dto.DepartmentTicketStatusResponse;
import com.wip.workipedia.admin.dashboard.dto.MonthlyAutoAssignmentRateResponse;
import com.wip.workipedia.admin.dashboard.dto.MonthlyTicketTrendResponse;
import com.wip.workipedia.admin.dashboard.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

	private final AdminDashboardService adminDashboardService;

	@GetMapping("/monthly-auto-assignment-rate")
	public ResponseEntity<MonthlyAutoAssignmentRateResponse> monthlyAutoAssignmentRate(
		@AuthenticationPrincipal Long actorUserId,
		@RequestParam(required = false) Integer months
	) {
		return ResponseEntity.ok(adminDashboardService.getMonthlyAutoAssignmentRate(actorUserId, months));
	}

	@GetMapping("/monthly-ticket-trend")
	public ResponseEntity<MonthlyTicketTrendResponse> monthlyTicketTrend(
		@AuthenticationPrincipal Long actorUserId,
		@RequestParam(required = false) Integer months
	) {
		return ResponseEntity.ok(adminDashboardService.getMonthlyTicketTrend(actorUserId, months));
	}

	@GetMapping("/department-ticket-status")
	public ResponseEntity<DepartmentTicketStatusResponse> departmentTicketStatus(
		@AuthenticationPrincipal Long actorUserId
	) {
		return ResponseEntity.ok(adminDashboardService.getDepartmentTicketStatus(actorUserId));
	}

	@GetMapping("/department-auto-assignment-rate")
	public ResponseEntity<DepartmentAutoAssignmentRateResponse> departmentAutoAssignmentRate(
		@AuthenticationPrincipal Long actorUserId
	) {
		return ResponseEntity.ok(adminDashboardService.getDepartmentAutoAssignmentRate(actorUserId));
	}
}
