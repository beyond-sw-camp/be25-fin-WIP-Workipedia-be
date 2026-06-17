package com.wip.workipedia.admin.aitool.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiToolCreateRequest(
	@NotBlank @Size(max = 100) String name,
	@NotBlank @Size(max = 1000) String description,
	@NotBlank String toolType,
	@Size(max = 1000) String endpointUrl,
	String httpMethod,
	@Size(max = 100) String datasourceKey,
	String queryTemplate,
	@NotBlank String parametersSchema,
	String responseSchema,
	@NotBlank String authType,
	String credentialRef,
	@Min(100) @Max(60000) int timeoutMs,
	@Min(1) @Max(1000) int maxResultCount
) {
}
