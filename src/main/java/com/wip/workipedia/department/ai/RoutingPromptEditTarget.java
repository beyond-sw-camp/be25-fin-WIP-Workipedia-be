package com.wip.workipedia.department.ai;

public record RoutingPromptEditTarget(
	Long departmentId,
	String departmentName,
	String currentPrompt
) {
}
