package com.wip.workipedia.chatbot.ai;

import java.util.List;

public record ChatbotAiRequest(
	String question,
	String customPrompt,
	List<SessionMessage> sessionContext
) {}
