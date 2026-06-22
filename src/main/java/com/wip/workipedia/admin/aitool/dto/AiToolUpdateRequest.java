package com.wip.workipedia.admin.aitool.dto;

public record AiToolUpdateRequest(
	String description,
	String endpointUrl,
	String httpMethod,
	String datasourceKey,
	String queryTemplate,
	String parametersSchema,
	String responseSchema,
	String authType,
	String credentialRef,
	Integer timeoutMs,
	Integer maxResultCount,
	String approvalStatus,
	Boolean active
) {
}
