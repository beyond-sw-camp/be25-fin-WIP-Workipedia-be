package com.wip.workipedia.ticket.service;

import com.wip.workipedia.ticket.dto.TicketDraftRequest;
import com.wip.workipedia.ticket.dto.TicketDraftResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * 사용자 요청 원문을 AI(LLM)로 보내 티켓 초안(제목/내용)으로 정리하는 얇은 프록시.
 * RAG가 아닌 가벼운 단일 호출이며, 실패하면 원문 그대로 fallback 해 요청 흐름이 깨지지 않게 한다.
 */
@Slf4j
@Service
public class TicketDraftService {

	private static final int MAX_TITLE_LEN = 50;

	private final RestClient routingAiRestClient;

	public TicketDraftService(@Qualifier("routingAiRestClient") RestClient routingAiRestClient) {
		this.routingAiRestClient = routingAiRestClient;
	}

	public TicketDraftResponse draft(TicketDraftRequest request) {
		try {
			TicketDraftResponse response = routingAiRestClient.post()
				.uri("/api/v1/tickets/draft")
				.body(request)
				.retrieve()
				.body(TicketDraftResponse.class);

			if (response == null || isBlank(response.title()) || isBlank(response.content())) {
				return fallback(request.rawText());
			}
			return response;
		} catch (Exception e) {
			log.warn("[티켓초안] AI 호출 실패 → 원문 fallback: {}", e.getMessage());
			return fallback(request.rawText());
		}
	}

	// AI 실패 시: 제목은 원문 첫 줄(최대 50자), 내용은 원문 그대로.
	private TicketDraftResponse fallback(String rawText) {
		String trimmed = rawText == null ? "" : rawText.strip();
		String firstLine = trimmed.isEmpty() ? "문의" : trimmed.lines().findFirst().orElse("문의");
		String title = firstLine.length() > MAX_TITLE_LEN ? firstLine.substring(0, MAX_TITLE_LEN) : firstLine;
		return new TicketDraftResponse(title, trimmed);
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
