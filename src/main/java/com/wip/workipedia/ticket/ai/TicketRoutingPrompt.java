package com.wip.workipedia.ticket.ai;

public record TicketRoutingPrompt(
	String title,
	String content,
	Long categoryId,
	Long questionId,
	Long sourceChatbotMessageId
) {
}
