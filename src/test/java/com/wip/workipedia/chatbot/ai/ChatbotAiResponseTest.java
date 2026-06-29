package com.wip.workipedia.chatbot.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class ChatbotAiResponseTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void step_history를_역직렬화한다() throws Exception {
		ChatbotAiResponse response = objectMapper.readValue("""
			{
			  "answer": "답변",
			  "sources": [],
			  "route": "AGGREGATED",
			  "action": null,
			  "step_history": [
			    {"step": "tool", "status": "ERROR", "error_message": "timeout"}
			  ]
			}
			""", ChatbotAiResponse.class);

		assertThat(response.route()).isEqualTo("AGGREGATED");
		assertThat(response.stepHistory()).singleElement().satisfies(item -> {
			assertThat(item.step()).isEqualTo("tool");
			assertThat(item.status()).isEqualTo("ERROR");
			assertThat(item.errorMessage()).isEqualTo("timeout");
		});
	}

	@Test
	void stream_done도_step_history를_역직렬화한다() throws Exception {
		ChatbotStreamDone done = objectMapper.readValue("""
			{
			  "sources": [],
			  "route": "AGGREGATED",
			  "action": null,
			  "step_history": [{"step": "final_answer", "status": "SUCCESS"}]
			}
			""", ChatbotStreamDone.class);

		assertThat(done.stepHistory()).singleElement()
			.extracting(StepHistoryItem::step)
			.isEqualTo("final_answer");
	}
}
