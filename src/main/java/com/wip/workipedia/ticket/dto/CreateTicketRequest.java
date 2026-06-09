package com.wip.workipedia.ticket.dto;

import com.wip.workipedia.ticket.domain.TicketPriority;
import jakarta.validation.constraints.NotBlank;

public record CreateTicketRequest(
	Long sourceChatbotMessageId,
	TicketPriority priority,
	@NotBlank String title,
	@NotBlank String content
) {
}
