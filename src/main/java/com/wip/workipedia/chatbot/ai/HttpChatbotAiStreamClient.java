package com.wip.workipedia.chatbot.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

// AI 서버의 스트리밍 엔드포인트(/api/v1/chat/stream)를 호출해 SSE 이벤트를 그대로 Flux로 흘려보낸다.
// 토큰 누적·DB 저장·예외 처리는 ChatbotService가 담당하고, 이 클래스는 "받아서 넘기는" 역할만 한다.
@Slf4j
@Component
public class HttpChatbotAiStreamClient {

	// ServerSentEvent<String>: event 이름(token/done)과 data(JSON 문자열)를 분리해서 받기 위한 타입
	private static final ParameterizedTypeReference<ServerSentEvent<String>> SSE_STRING =
		new ParameterizedTypeReference<>() {};

	private final WebClient chatbotAiWebClient;

	public HttpChatbotAiStreamClient(@Qualifier("chatbotAiWebClient") WebClient chatbotAiWebClient) {
		this.chatbotAiWebClient = chatbotAiWebClient;
	}

	public Flux<ServerSentEvent<String>> askStream(ChatbotAiRequest request) {
		return chatbotAiWebClient.post()
			.uri("/api/v1/chat/stream")
			.bodyValue(request)
			.retrieve()
			.bodyToFlux(SSE_STRING);
	}
}
