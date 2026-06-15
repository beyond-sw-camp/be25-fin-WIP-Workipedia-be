package com.wip.workipedia.chatbot.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatbotAiResponse(
	String answer,
	List<SourceItem> sources,
	String route,
	String action
) {}
