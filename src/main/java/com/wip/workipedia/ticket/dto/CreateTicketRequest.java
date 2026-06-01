package com.wip.workipedia.ticket.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTicketRequest(
	Long questionId,
	Long sourceChatbotMessageId,
	Long categoryId,
	@NotBlank String title,
	@NotBlank String content
) {
}
