package com.wip.workipedia.department.ai;

import java.util.List;

public record AiRoutingPromptRequest(
	String instruction,
	List<RoutingPromptEditTarget> targets
) {}
