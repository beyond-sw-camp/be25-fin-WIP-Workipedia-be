package com.wip.workipedia.tool.dto;

import com.wip.workipedia.tool.domain.AiTool;

public record ActiveAiToolResponse(
	Long aiToolId,
	String toolType,
	String name,
	String description,
	String parametersSchema
) {
	public static ActiveAiToolResponse from(AiTool tool) {
		return new ActiveAiToolResponse(
			tool.getAiToolId(),
			tool.getToolType().name(),
			tool.getName(),
			tool.getDescription(),
			tool.getParametersSchema()
		);
	}
}
