package com.wip.workipedia.chatbot.ai;

import java.util.List;

public record ChatbotAiRequest(
	String question,
	String customPrompt,
	List<SessionMessage> sessionContext,
	String callerEmployeeId
) {
	public ChatbotAiRequest(String question, String customPrompt, List<SessionMessage> sessionContext) {
		this(question, customPrompt, sessionContext, null);
	}
}
