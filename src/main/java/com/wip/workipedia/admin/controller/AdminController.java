package com.wip.workipedia.admin.controller;

import com.wip.workipedia.admin.dto.AdminDashboardResponse;
import com.wip.workipedia.admin.dto.AdminLogResponse;
import com.wip.workipedia.admin.dto.AdminTicketQueueResponse;
import com.wip.workipedia.admin.dto.KnowledgeCandidateSummaryResponse;
import com.wip.workipedia.admin.service.AdminDashboardService;
import com.wip.workipedia.common.response.ApiResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

	private final AdminDashboardService adminDashboardService;

	public AdminController(AdminDashboardService adminDashboardService) {
		this.adminDashboardService = adminDashboardService;
	}

	@GetMapping("/dashboard")
	public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
		return ApiResponse.success(HttpStatus.OK, "관리자 대시보드 조회 성공", adminDashboardService.getDashboard());
	}

	@GetMapping("/team/tickets")
	public ResponseEntity<ApiResponse<List<AdminTicketQueueResponse>>> getTeamTickets() {
		return ApiResponse.success(HttpStatus.OK, "팀 티켓 큐 조회 성공", adminDashboardService.getTeamTickets());
	}

	@GetMapping("/team/knowledge-candidates")
	public ResponseEntity<ApiResponse<List<KnowledgeCandidateSummaryResponse>>> getKnowledgeCandidates() {
		return ApiResponse.success(HttpStatus.OK, "지식화 후보 조회 성공", adminDashboardService.getKnowledgeCandidates());
	}

	@GetMapping("/common-queue/tickets")
	public ResponseEntity<ApiResponse<List<AdminTicketQueueResponse>>> getCommonQueueTickets() {
		return ApiResponse.success(HttpStatus.OK, "공통 접수 큐 조회 성공", adminDashboardService.getCommonQueueTickets());
	}

	@GetMapping("/logs")
	public ResponseEntity<ApiResponse<List<AdminLogResponse>>> getAdminLogs() {
		return ApiResponse.success(HttpStatus.OK, "관리자 작업 로그 조회 성공", adminDashboardService.getAdminLogs());
	}
}
