package com.wip.workipedia.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TicketAnswerCreateRequest(
	@NotBlank
	@Size(max = 5000)
	String content,

	@Size(max = 500)
	String fileKey
) {
	public TicketAnswerCreateRequest(String content) {
		this(content, null);
	}
}
