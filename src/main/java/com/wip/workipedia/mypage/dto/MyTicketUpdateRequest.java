package com.wip.workipedia.mypage.dto;

import jakarta.validation.constraints.NotBlank;

public record MyTicketUpdateRequest(
	@NotBlank String title,
	@NotBlank String content
) {
}
