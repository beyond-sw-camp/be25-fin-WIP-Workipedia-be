package com.wip.workipedia.department.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiRoutingPromptResponse(
	List<AiRoutingPromptResult> results
) {
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record AiRoutingPromptResult(
		Long departmentId,
		String routingPrompt
	) {}
}
