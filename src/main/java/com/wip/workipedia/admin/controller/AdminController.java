package com.wip.workipedia.admin.controller;

import com.wip.workipedia.admin.dto.AdminDashboardResponse;
import com.wip.workipedia.admin.dto.AdminLogResponse;
import com.wip.workipedia.admin.dto.AdminTicketQueueResponse;
import com.wip.workipedia.admin.dto.KnowledgeReviewTicketResponse;
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

	// SYSTEM_ADMIN 전체 운영 현황 API
	@GetMapping("/dashboard")
	public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
		return ApiResponse.success(HttpStatus.OK, "관리자 대시보드 조회 성공", adminDashboardService.getDashboard());
	}

	// TEAM_ADMIN이 자기 팀에 배정된 요청 티켓을 확인 API
	@GetMapping("/team/tickets")
	public ResponseEntity<ApiResponse<List<AdminTicketQueueResponse>>> getTeamTickets() {
		return ApiResponse.success(HttpStatus.OK, "팀 티켓 큐 조회 성공", adminDashboardService.getTeamTickets());
	}

	// TEAM_ADMIN이 처리 완료된 부서 티켓을 보고 지식화 여부를 선택하기 위한 목록
	@GetMapping("/team/knowledge-review-tickets")
	public ResponseEntity<ApiResponse<List<KnowledgeReviewTicketResponse>>> getKnowledgeReviewTickets() {
		return ApiResponse.success(HttpStatus.OK, "지식화 검토 대상 조회 성공", adminDashboardService.getKnowledgeReviewTickets());
	}

	// SYSTEM_ADMIN이 자동 배정되지 못한 티켓을 보고 수동 배정하기 위한 큐
	@GetMapping("/common-queue/tickets")
	public ResponseEntity<ApiResponse<List<AdminTicketQueueResponse>>> getCommonQueueTickets() {
		return ApiResponse.success(HttpStatus.OK, "공통 접수 큐 조회 성공", adminDashboardService.getCommonQueueTickets());
	}

	// 관리자 작업 이력을 확인하는 API , 실제 기록 생성은 각 관리자 기능에서 연결
	@GetMapping("/logs")
	public ResponseEntity<ApiResponse<List<AdminLogResponse>>> getAdminLogs() {
		return ApiResponse.success(HttpStatus.OK, "관리자 작업 로그 조회 성공", adminDashboardService.getAdminLogs());
	}
}
