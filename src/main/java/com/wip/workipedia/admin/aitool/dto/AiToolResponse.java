package com.wip.workipedia.admin.aitool.dto;

import com.wip.workipedia.tool.domain.AiTool;
import java.time.LocalDateTime;

public record AiToolResponse(
	Long aiToolId,
	String name,
	String description,
	String toolType,
	String endpointUrl,
	String httpMethod,
	String datasourceKey,
	String queryTemplate,
	String authType,
	String credentialRef,
	int timeoutMs,
	int maxResultCount,
	String approvalStatus,
	boolean active,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static AiToolResponse from(AiTool tool) {
		return new AiToolResponse(
			tool.getAiToolId(),
			tool.getName(),
			tool.getDescription(),
			tool.getToolType().name(),
			tool.getEndpointUrl(),
			tool.getHttpMethod(),
			tool.getDatasourceKey(),
			tool.getQueryTemplate(),
			tool.getAuthType().name(),
			tool.getCredentialRef(),
			tool.getTimeoutMs(),
			tool.getMaxResultCount(),
			tool.getApprovalStatus().name(),
			tool.isActive(),
			tool.getCreatedAt(),
			tool.getUpdatedAt()
		);
	}
}
