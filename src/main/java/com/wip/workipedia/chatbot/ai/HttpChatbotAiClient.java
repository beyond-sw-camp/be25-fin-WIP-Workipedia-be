package com.wip.workipedia.chatbot.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Primary
@Component
public class HttpChatbotAiClient implements ChatbotAiClient {

    private final RestClient chatbotAiRestClient;
    private final FallbackChatbotAiClient fallback;

    public HttpChatbotAiClient(
            @Qualifier("chatbotAiRestClient") RestClient chatbotAiRestClient,
            FallbackChatbotAiClient fallback) {
        this.chatbotAiRestClient = chatbotAiRestClient;
        this.fallback = fallback;
    }

    @Override
    public ChatbotAiResponse ask(ChatbotAiRequest request) {
        try {
            ChatbotAiResponse response = chatbotAiRestClient.post()
                    .uri("/api/v1/chat")
                    .body(request)
                    .retrieve()
                    .body(ChatbotAiResponse.class);

            if (response == null) {
                log.error("AI 챗봇 응답이 null입니다.");
                return fallback.ask(request);
            }
            return response;
        } catch (Exception e) {
            log.error("AI 챗봇 호출 실패: {}", e.getMessage());
            return fallback.ask(request);
        }
    }
}
