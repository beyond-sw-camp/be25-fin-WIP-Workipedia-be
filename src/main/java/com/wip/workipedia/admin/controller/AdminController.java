package com.wip.workipedia.admin.controller;

import com.wip.workipedia.admin.dto.AdminDashboardResponse;
import com.wip.workipedia.admin.dto.AdminLogResponse;
import com.wip.workipedia.admin.dto.AdminTicketQueueResponse;
import com.wip.workipedia.admin.dto.KnowledgeReviewTicketResponse;
import com.wip.workipedia.admin.service.AdminDashboardService;
import com.wip.workipedia.esg.dto.AdminEsgResponse;
import com.wip.workipedia.esg.service.EsgService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

	private final AdminDashboardService adminDashboardService;
	private final EsgService esgService;

	// SYSTEM_ADMIN 전체 운영 현황 API
	@GetMapping("/dashboard")
	public ResponseEntity<AdminDashboardResponse> getDashboard() {
		return ResponseEntity.ok(adminDashboardService.getDashboard());
	}

	// TEAM_ADMIN이 자기 팀에 배정된 요청 티켓을 확인 API
	@GetMapping("/team/tickets")
	public ResponseEntity<List<AdminTicketQueueResponse>> getTeamTickets() {
		return ResponseEntity.ok(adminDashboardService.getTeamTickets());
	}

	// TEAM_ADMIN이 처리 완료된 부서 티켓을 보고 지식화 여부를 선택하기 위한 목록
	@GetMapping("/team/knowledge-review-tickets")
	public ResponseEntity<List<KnowledgeReviewTicketResponse>> getKnowledgeReviewTickets() {
		return ResponseEntity.ok(adminDashboardService.getKnowledgeReviewTickets());
	}

	// SYSTEM_ADMIN이 자동 배정되지 못한 티켓을 보고 수동 배정하기 위한 큐
	@GetMapping("/common-queue/tickets")
	public ResponseEntity<List<AdminTicketQueueResponse>> getCommonQueueTickets() {
		return ResponseEntity.ok(adminDashboardService.getCommonQueueTickets());
	}

	// 관리자 작업 이력을 확인하는 API , 실제 기록 생성은 각 관리자 기능에서 연결
	@GetMapping("/logs")
	public ResponseEntity<List<AdminLogResponse>> getAdminLogs() {
		return ResponseEntity.ok(adminDashboardService.getAdminLogs());
	}

	@GetMapping("/esg")
	public ResponseEntity<AdminEsgResponse> getAdminEsg() {
		return ResponseEntity.ok(esgService.getAdminEsg());
	}
}
