package com.wip.workipedia.admin.team.knowledge.dto;

import java.time.LocalDateTime;

public record KnowledgeTicketCandidateResponse(
	Long ticketId,
	Long departmentId,
	String question,
	String answer,
	Long answerId,
	Long answerAuthorId,
	LocalDateTime completedAt,
	LocalDateTime answeredAt
) {
}
