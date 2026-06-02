package com.wip.workipedia.ticket.dto;

import com.wip.workipedia.ticket.domain.TicketStatus;

public record TicketAssigneeResponse(
	Long ticketId,
	TicketStatus status,
	Long assigneeId,
	String assigneeNickname
) {
}
