package com.wip.workipedia.chatbot.ai;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FallbackChatbotAiClient implements ChatbotAiClient {

	@Override
	public ChatbotAiResponse ask(ChatbotAiRequest request) {
		return new ChatbotAiResponse(
			"현재 AI 서비스를 이용할 수 없습니다. 잠시 후 다시 시도하거나 티켓을 등록해 주세요.",
			List.of(),
			null,
			null,
			List.of()
		);
	}
}
