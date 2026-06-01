package com.wip.workipedia.admin.service;

import com.wip.workipedia.admin.dto.AdminDashboardResponse;
import com.wip.workipedia.admin.dto.AdminLogResponse;
import com.wip.workipedia.admin.dto.AdminTicketQueueResponse;
import com.wip.workipedia.admin.dto.KnowledgeCandidateSummaryResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardService {

	public AdminDashboardResponse getDashboard() {
		return new AdminDashboardResponse(
			7,
			3,
			18,
			5,
			2,
			0.82,
			240
		);
	}

	public List<AdminTicketQueueResponse> getTeamTickets() {
		return List.of(
			new AdminTicketQueueResponse(
				1L,
				"VPN 접속 오류 처리 요청",
				"ASSIGNED",
				5L,
				"IT지원팀",
				12L,
				"노잇4821",
				BigDecimal.valueOf(87.5),
				"AUTO_ASSIGNED",
				LocalDateTime.now().minusHours(2)
			)
		);
	}

	public List<AdminTicketQueueResponse> getCommonQueueTickets() {
		return List.of(
			new AdminTicketQueueResponse(
				2L,
				"SSD 추가 장착 가능 여부 확인",
				"COMMON_QUEUE",
				null,
				null,
				null,
				null,
				BigDecimal.valueOf(63.0),
				"COMMON_QUEUE",
				LocalDateTime.now().minusHours(1)
			)
		);
	}

	public List<KnowledgeCandidateSummaryResponse> getKnowledgeCandidates() {
		return List.of(
			new KnowledgeCandidateSummaryResponse(
				1L,
				1L,
				"VPN 접속 오류 처리 절차",
				"REVIEW_REQUESTED",
				LocalDateTime.now().minusMinutes(30)
			)
		);
	}

	public List<AdminLogResponse> getAdminLogs() {
		return List.of(
			new AdminLogResponse(
				1L,
				10L,
				"COMMON_QUEUE_ASSIGN",
				"TICKET",
				2L,
				"공통 접수 큐 티켓을 자산관리팀으로 배정",
				LocalDateTime.now().minusMinutes(20)
			)
		);
	}
}
