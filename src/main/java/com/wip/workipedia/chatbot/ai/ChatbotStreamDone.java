package com.wip.workipedia.chatbot.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

// AI 서버 스트림의 done 이벤트 data — 토큰이 모두 끝난 뒤 메타데이터만 한 번 전달된다.
// 예: event: done / data: {"sources":[...],"route":"...","action":"CREATE_TICKET"}
// 답을 찾지 못한 경우 sources는 빈 배열, action은 null로 온다(=빈 답변 처리 트리거).
@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatbotStreamDone(
	List<SourceItem> sources,
	String route,
	String action,
	@JsonProperty("step_history") List<StepHistoryItem> stepHistory
) {
	public static ChatbotStreamDone empty() {
		return new ChatbotStreamDone(List.of(), null, null, List.of());
	}
}
