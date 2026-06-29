package com.wip.workipedia.chatbot.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatbotAiResponse(
	String answer,
	List<SourceItem> sources,
	String route,
	String action,
	@JsonProperty("step_history") List<StepHistoryItem> stepHistory
) {
	public ChatbotAiResponse(String answer, List<SourceItem> sources, String route, String action) {
		this(answer, sources, route, action, List.of());
	}
}
