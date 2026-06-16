package com.wip.workipedia.ticket.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.wip.workipedia.ticket.domain.RoutingDecision;
import com.wip.workipedia.ticket.dto.RoutingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class HttpTicketRoutingAiClientTest {

	private MockRestServiceServer mockServer;
	private HttpTicketRoutingAiClient client;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder().baseUrl("http://ai-server");
		mockServer = MockRestServiceServer.bindTo(builder).build();
		FallbackTicketRoutingAiClient fallback = new FallbackTicketRoutingAiClient();
		client = new HttpTicketRoutingAiClient(builder.build(), fallback);
	}

	@Test
	void AI가_AUTO_ASSIGNED_반환하면_그대로_사용() {
		mockServer.expect(requestTo("http://ai-server/api/v1/tickets/routing"))
			.andRespond(withSuccess("""
				{
				  "assignedDepartmentId": 2,
				  "assignedDepartmentName": "개발팀",
				  "confidenceScore": 3.5,
				  "scoreMargin": 1.2,
				  "decision": "AUTO_ASSIGNED",
				  "reasons": ["R&R 매칭"],
				  "candidateDepartments": [
				    { "departmentId": 2, "departmentName": "개발팀", "confidenceScore": 3.5 }
				  ],
				  "model": "cross-encoder-v1",
				  "provider": "local"
				}
				""", MediaType.APPLICATION_JSON));

		RoutingResult result = client.recommend(new TicketRoutingPrompt("제목", "내용", null));

		assertThat(result.decision()).isEqualTo(RoutingDecision.AUTO_ASSIGNED);
		assertThat(result.assignedDepartmentId()).isEqualTo(2L);
		assertThat(result.assignedDepartmentName()).isEqualTo("개발팀");
		assertThat(result.confidenceScore()).isEqualByComparingTo("3.5");
		assertThat(result.scoreMargin()).isEqualByComparingTo("1.2");
		assertThat(result.modelVersion()).isEqualTo("cross-encoder-v1@local");
		assertThat(result.candidateDepartments()).hasSize(1);
	}

	@Test
	void AI가_COMMON_QUEUE_반환하면_assignedDepartmentId_null() {
		mockServer.expect(requestTo("http://ai-server/api/v1/tickets/routing"))
			.andRespond(withSuccess("""
				{
				  "assignedDepartmentId": null,
				  "assignedDepartmentName": null,
				  "confidenceScore": 1.0,
				  "scoreMargin": 0.2,
				  "decision": "COMMON_QUEUE",
				  "reasons": ["점수 미달"],
				  "candidateDepartments": [],
				  "model": "cross-encoder-v1",
				  "provider": "local"
				}
				""", MediaType.APPLICATION_JSON));

		RoutingResult result = client.recommend(new TicketRoutingPrompt("제목", "내용", null));

		assertThat(result.decision()).isEqualTo(RoutingDecision.COMMON_QUEUE);
		assertThat(result.assignedDepartmentId()).isNull();
	}

	@Test
	void AI_서버_오류_시_Fallback으로_COMMON_QUEUE() {
		mockServer.expect(requestTo("http://ai-server/api/v1/tickets/routing"))
			.andRespond(withServerError());

		RoutingResult result = client.recommend(new TicketRoutingPrompt("제목", "내용", null));

		assertThat(result.decision()).isEqualTo(RoutingDecision.COMMON_QUEUE);
		assertThat(result.reasons()).isNotEmpty();
	}

	@Test
	void 알_수_없는_decision_값은_COMMON_QUEUE로_처리() {
		mockServer.expect(requestTo("http://ai-server/api/v1/tickets/routing"))
			.andRespond(withSuccess("""
				{
				  "assignedDepartmentId": null,
				  "decision": "UNKNOWN_VALUE",
				  "reasons": [],
				  "candidateDepartments": [],
				  "model": "v1",
				  "provider": "local"
				}
				""", MediaType.APPLICATION_JSON));

		RoutingResult result = client.recommend(new TicketRoutingPrompt("제목", "내용", null));

		assertThat(result.decision()).isEqualTo(RoutingDecision.COMMON_QUEUE);
	}
}
