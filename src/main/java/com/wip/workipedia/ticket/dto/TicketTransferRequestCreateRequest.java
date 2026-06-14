package com.wip.workipedia.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TicketTransferRequestCreateRequest(
	@NotBlank
	@Size(max = 1000)
	String reason,

	Long suggestedDepartmentId
) {
}
