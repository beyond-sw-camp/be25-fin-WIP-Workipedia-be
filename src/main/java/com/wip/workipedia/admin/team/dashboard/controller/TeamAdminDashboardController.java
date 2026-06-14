package com.wip.workipedia.admin.team.dashboard.controller;

import com.wip.workipedia.admin.team.dashboard.dto.MonthlyTrendResponse;
import com.wip.workipedia.admin.team.dashboard.service.TeamAdminDashboardService;
import com.wip.workipedia.team.dto.TeamTicketSummaryResponse;
import com.wip.workipedia.team.service.TeamTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/team/dashboard")
@RequiredArgsConstructor
public class TeamAdminDashboardController {

	private final TeamAdminDashboardService teamAdminDashboardService;
	private final TeamTicketService teamTicketService;

	@GetMapping("/summary")
	public ResponseEntity<TeamTicketSummaryResponse> summary(
		@AuthenticationPrincipal Long actorUserId
	) {
		return ResponseEntity.ok(teamTicketService.getSummary(actorUserId));
	}

	@GetMapping("/knowledge-trend")
	public ResponseEntity<MonthlyTrendResponse> knowledgeTrend(
		@AuthenticationPrincipal Long actorUserId,
		@RequestParam(required = false) Integer months
	) {
		return ResponseEntity.ok(teamAdminDashboardService.getKnowledgeTrend(actorUserId, months));
	}

	@GetMapping("/chatbot-ticket-trend")
	public ResponseEntity<MonthlyTrendResponse> chatbotTicketTrend(
		@AuthenticationPrincipal Long actorUserId,
		@RequestParam(required = false) Integer months
	) {
		return ResponseEntity.ok(teamAdminDashboardService.getChatbotAssignmentTrend(actorUserId, months));
	}
}
