package com.wip.workipedia.ticket.dto;

import jakarta.validation.constraints.NotNull;

public record TicketAssigneeRequest(
	@NotNull Long assigneeId,
	String memo
) {
}
