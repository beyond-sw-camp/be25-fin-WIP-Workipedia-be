package com.wip.workipedia.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record TicketAnswerCreateRequest(
	@NotBlank
	@Size(max = 5000)
	String content,

	@Size(max = 500)
	String fileKey,

	@Size(max = 1000)
	String fileUrl,

	@Size(max = 255)
	String fileName,

	@Size(max = 100)
	String fileContentType,

	@PositiveOrZero
	Long fileSize
) {
	public TicketAnswerCreateRequest(String content) {
		this(content, null, null, null, null, null);
	}
}
