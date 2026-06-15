package com.wip.workipedia.chatbot.ai;

public record SessionMessage(
	Long messageId,
	String senderType,
	String content
) {}
