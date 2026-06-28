package com.wip.workipedia.admin.aitool.dto;

import jakarta.validation.constraints.NotBlank;

public record AiToolDraftRequest(
	@NotBlank String endpointUrl,
	String httpMethod
) {
}
