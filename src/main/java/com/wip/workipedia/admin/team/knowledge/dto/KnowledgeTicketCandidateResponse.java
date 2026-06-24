package com.wip.workipedia.admin.team.knowledge.dto;

import java.time.LocalDateTime;
import java.util.List;
import com.wip.workipedia.ticket.dto.TicketFileResponse;

public record KnowledgeTicketCandidateResponse(
	Long ticketId,
	Long departmentId,
	String question,
	String answer,
	Long answerId,
	Long answerAuthorId,
	LocalDateTime completedAt,
	LocalDateTime answeredAt,
	String fileUrl,
	List<TicketFileResponse> files
) {
}
