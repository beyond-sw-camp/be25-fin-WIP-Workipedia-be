package com.wip.workipedia.ticket.dto;

import com.wip.workipedia.ticket.domain.TicketPriority;
import jakarta.validation.constraints.NotBlank;

public record CreateTicketRequest(
	Long questionId,
	Long sourceChatbotMessageId,
	Long categoryId,
	TicketPriority priority,
	@NotBlank String title,
	@NotBlank String content
) {
}
