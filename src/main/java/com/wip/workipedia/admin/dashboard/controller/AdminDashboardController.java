package com.wip.workipedia.admin.dashboard.controller;

import com.wip.workipedia.admin.dashboard.dto.DepartmentAutoAssignmentRateResponse;
import com.wip.workipedia.admin.dashboard.dto.DepartmentTicketStatusResponse;
import com.wip.workipedia.admin.dashboard.dto.LlmUsageSavingsResponse;
import com.wip.workipedia.admin.dashboard.dto.MonthlyAutoAssignmentRateResponse;
import com.wip.workipedia.admin.dashboard.dto.MonthlyTicketTrendResponse;
import com.wip.workipedia.admin.dashboard.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
@RequiredArgsConstructor
public class AdminDashboardController {

	private final AdminDashboardService adminDashboardService;

	@GetMapping("/monthly-auto-assignment-rate")
	public ResponseEntity<MonthlyAutoAssignmentRateResponse> monthlyAutoAssignmentRate(
		@RequestParam(required = false) Integer months
	) {
		return ResponseEntity.ok(adminDashboardService.getMonthlyAutoAssignmentRate(months));
	}

	@GetMapping("/monthly-ticket-trend")
	public ResponseEntity<MonthlyTicketTrendResponse> monthlyTicketTrend(
		@RequestParam(required = false) Integer months
	) {
		return ResponseEntity.ok(adminDashboardService.getMonthlyTicketTrend(months));
	}

	@GetMapping("/department-ticket-status")
	public ResponseEntity<DepartmentTicketStatusResponse> departmentTicketStatus() {
		return ResponseEntity.ok(adminDashboardService.getDepartmentTicketStatus());
	}

	@GetMapping("/department-auto-assignment-rate")
	public ResponseEntity<DepartmentAutoAssignmentRateResponse> departmentAutoAssignmentRate() {
		return ResponseEntity.ok(adminDashboardService.getDepartmentAutoAssignmentRate());
	}

	@GetMapping("/llm-usage-savings")
	public ResponseEntity<LlmUsageSavingsResponse> llmUsageSavings() {
		return ResponseEntity.ok(adminDashboardService.getLlmUsageSavings());
	}
}
