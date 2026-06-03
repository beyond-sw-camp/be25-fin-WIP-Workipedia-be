package com.wip.workipedia.ticket.dto;

import com.wip.workipedia.ticket.domain.RoutingDecision;
import com.wip.workipedia.ticket.domain.Ticket;
import com.wip.workipedia.ticket.domain.TicketStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TicketResponse(
	Long ticketId,
	TicketStatus status,
	Long assignedDepartmentId,
	String assignedDepartmentName,
	BigDecimal routingConfidenceScore,
	RoutingDecision routingDecision,
	List<String> routingReasons,
	List<CandidateDepartmentResponse> candidateDepartments,
	Long questionId,
	Long sourceChatbotMessageId,
	Long categoryId,
	String title,
	String content,
	Long assigneeId,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static TicketResponse from(Ticket ticket, RoutingResult routingResult) {
		return new TicketResponse(
			ticket.getTicketId(),
			ticket.getStatus(),
			ticket.getAssignedDepartmentId(),
			null, // TODO: 부서 엔티티 조회 후 부서명 반환 (Department 도메인 구현 후 연결)
			ticket.getRoutingConfidenceScore(),
			ticket.getRoutingDecision(),
			routingResult.reasons(),
			routingResult.candidateDepartments(),
			ticket.getQuestionId(),
			ticket.getSourceChatbotMessageId(),
			ticket.getCategoryId(),
			ticket.getTitle(),
			ticket.getContent(),
			ticket.getAssigneeId(),
			ticket.getCreatedAt(),
			ticket.getUpdatedAt()
		);
	}
}
