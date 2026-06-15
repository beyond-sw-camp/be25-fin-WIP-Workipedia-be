package com.wip.workipedia.department.dto;

import jakarta.validation.constraints.NotBlank;

public record RoutingPromptDirectEditRequest(
	@NotBlank String routingPrompt
) {}
