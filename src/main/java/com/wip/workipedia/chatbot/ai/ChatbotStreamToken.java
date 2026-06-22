package com.wip.workipedia.chatbot.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// AI 서버 스트림의 token 이벤트 data, 그리고 Spring→프론트로 다시 내보내는 token 이벤트 data.
// 예: event: token / data: {"content":"안녕"}
@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatbotStreamToken(
	String content
) {}
