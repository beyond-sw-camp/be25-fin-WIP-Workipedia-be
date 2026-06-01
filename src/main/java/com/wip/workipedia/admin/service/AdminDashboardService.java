package com.wip.workipedia.admin.service;

import com.wip.workipedia.admin.dto.AdminDashboardResponse;
import com.wip.workipedia.admin.dto.AdminLogResponse;
import com.wip.workipedia.admin.dto.AdminTicketQueueResponse;
import com.wip.workipedia.admin.dto.KnowledgeReviewTicketResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardService {

	// Week 1 skeleton 단계에서는 실제 repository 연동 전까지 빈 값을 반환합니다.
	// 티켓/지식화/ESG 집계 로직이 들어오면 repository 조회로 교체합니다.
	public AdminDashboardResponse getDashboard() {
		return new AdminDashboardResponse(
			0,
			0,
			0,
			0,
			0,
			0.0
		);
	}

	// TEAM_ADMIN의 팀 큐 목록입니다. 실제 티켓 repository 연동 전까지는 빈 목록입니다.
	public List<AdminTicketQueueResponse> getTeamTickets() {
		return List.of();
	}

	// SYSTEM_ADMIN의 공통 접수 큐 목록입니다. 실제 티켓 repository 연동 전까지는 빈 목록입니다.
	public List<AdminTicketQueueResponse> getCommonQueueTickets() {
		return List.of();
	}

	// 처리 완료된 부서 티켓은 TEAM_ADMIN의 지식화 검토 대상이 됩니다.
	public List<KnowledgeReviewTicketResponse> getKnowledgeReviewTickets() {
		return List.of();
	}

	// 관리자 작업 로그 목록입니다. 실제 admin log repository 연동 전까지는 빈 목록입니다.
	public List<AdminLogResponse> getAdminLogs() {
		return List.of();
	}
}
