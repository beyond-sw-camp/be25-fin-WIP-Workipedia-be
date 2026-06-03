package com.wip.workipedia.ticket.dto;

import com.wip.workipedia.ticket.domain.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record TicketStatusRequest(
	@NotNull TicketStatus status
) {
}
