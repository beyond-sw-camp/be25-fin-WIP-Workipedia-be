package com.wip.workipedia.ticket.dto;

import com.wip.workipedia.ticket.domain.RoutingDecision;
import com.wip.workipedia.ticket.domain.Ticket;
import com.wip.workipedia.ticket.domain.TicketPriority;
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
	Long sourceChatbotMessageId,
	TicketPriority priority,
	String title,
	String content,
	Long assigneeId,
	String transferReason,
	Long transferSuggestedDepartmentId,
	String transferSuggestedDepartmentName,
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
			ticket.getSourceChatbotMessageId(),
			ticket.getPriority(),
			ticket.getTitle(),
			ticket.getContent(),
			ticket.getAssigneeId(),
			null,
			null,
			null,
			ticket.getCreatedAt(),
			ticket.getUpdatedAt()
		);
	}

	public TicketResponse withTransferInfo(
		String transferReason,
		Long transferSuggestedDepartmentId,
		String transferSuggestedDepartmentName
	) {
		return new TicketResponse(
			ticketId,
			status,
			assignedDepartmentId,
			assignedDepartmentName,
			routingConfidenceScore,
			routingDecision,
			routingReasons,
			candidateDepartments,
			sourceChatbotMessageId,
			priority,
			title,
			content,
			assigneeId,
			transferReason,
			transferSuggestedDepartmentId,
			transferSuggestedDepartmentName,
			createdAt,
			updatedAt
		);
	}
}
