package com.wip.workipedia.chatbot.ai;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class FallbackChatbotAiClientTest {

	@Test
	void Fallback은_AI_불가_안내_메시지를_반환한다() {
		FallbackChatbotAiClient client = new FallbackChatbotAiClient();
		ChatbotAiResponse response = client.ask(new ChatbotAiRequest("질문", null, List.of()));
		assertThat(response.answer()).contains("AI 서비스");
		assertThat(response.sources()).isEmpty();
		assertThat(response.action()).isNull();
	}
}
