package com.wip.workipedia.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("ai")
public record AiProperties(
	String baseUrl,
	Timeout timeout
) {
	public record Timeout(long routingMs, long chatbotMs) {}
}
