package com.wip.workipedia.admin.dto;

import java.time.LocalDateTime;

// TEAM_ADMIN이 지식화 여부를 검토할 처리 완료 티켓 요약 정보입니다.
public record KnowledgeReviewTicketResponse(
	long ticketId,
	String ticketTitle,
	String answerSummary,
	String knowledgeReviewStatus,
	LocalDateTime completedAt
) {
}
