package com.wip.workipedia.ticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.wip.workipedia.ticket.dto.TicketDraftRequest;
import com.wip.workipedia.ticket.dto.TicketDraftResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class TicketDraftServiceTest {

	private MockRestServiceServer server;
	private TicketDraftService service;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder();
		server = MockRestServiceServer.bindTo(builder).build();
		service = new TicketDraftService(builder.build());
	}

	@Test
	void draft_returnsAiDraft() {
		server.expect(requestTo("/api/v1/tickets/draft"))
			.andRespond(withSuccess(
				"{\"title\":\"연차 잔여일수 문의\",\"content\":\"올해 잔여 연차를 확인하고 싶습니다.\"}",
				MediaType.APPLICATION_JSON));

		TicketDraftResponse res = service.draft(new TicketDraftRequest("올해 연차 얼마나 써야돼?"));

		assertThat(res.title()).isEqualTo("연차 잔여일수 문의");
		assertThat(res.content()).contains("연차");
		server.verify();
	}

	@Test
	void draft_aiError_fallsBackToRawText() {
		server.expect(requestTo("/api/v1/tickets/draft")).andRespond(withServerError());

		TicketDraftResponse res = service.draft(new TicketDraftRequest("비품 신청하고 싶어요"));

		// AI 실패 시 원문을 content로, 제목은 비지 않게 fallback.
		assertThat(res.content()).isEqualTo("비품 신청하고 싶어요");
		assertThat(res.title()).isNotBlank();
	}

	@Test
	void draft_blankAiTitle_fallsBackToRawText() {
		server.expect(requestTo("/api/v1/tickets/draft"))
			.andRespond(withSuccess("{\"title\":\"\",\"content\":\"\"}", MediaType.APPLICATION_JSON));

		TicketDraftResponse res = service.draft(new TicketDraftRequest("계정 잠김 풀어주세요"));

		assertThat(res.content()).isEqualTo("계정 잠김 풀어주세요");
		assertThat(res.title()).isNotBlank();
	}
}
