package com.wip.workipedia.ticket.ai;

import com.wip.workipedia.ticket.domain.RoutingDecision;
import com.wip.workipedia.ticket.dto.CandidateDepartmentResponse;
import com.wip.workipedia.ticket.dto.RoutingResult;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Primary
@Component
public class HttpTicketRoutingAiClient implements TicketRoutingAiClient {

	private final RestClient routingAiRestClient;
	private final FallbackTicketRoutingAiClient fallback;

	public HttpTicketRoutingAiClient(
		@Qualifier("routingAiRestClient") RestClient routingAiRestClient,
		FallbackTicketRoutingAiClient fallback
	) {
		this.routingAiRestClient = routingAiRestClient;
		this.fallback = fallback;
	}

	@Override
	public RoutingResult recommend(TicketRoutingPrompt prompt) {
		try {
			AiRoutingResponse response = routingAiRestClient.post()
				.uri("/api/v1/tickets/routing")
				.body(prompt)
				.retrieve()
				.body(AiRoutingResponse.class);

			if (response == null) {
				log.error("AI 라우팅 응답이 null입니다.");
				return fallback.recommend(prompt);
			}

			return toRoutingResult(response);
		} catch (Exception e) {
			log.error("AI 라우팅 호출 실패: {}", e.getMessage());
			return fallback.recommend(prompt);
		}
	}

	private RoutingResult toRoutingResult(AiRoutingResponse response) {
		RoutingDecision decision = parseDecision(response.decision());

		List<CandidateDepartmentResponse> candidates = response.candidateDepartments() == null ? List.of() :
			response.candidateDepartments().stream()
				.map(c -> new CandidateDepartmentResponse(c.departmentId(), c.departmentName(), c.confidenceScore()))
				.toList();

		return new RoutingResult(
			response.assignedDepartmentId(),
			response.assignedDepartmentName(),
			response.confidenceScore(),
			response.scoreMargin(),
			buildModelVersion(response.model(), response.provider()),
			decision,
			response.reasons() == null ? List.of() : response.reasons(),
			candidates
		);
	}

	private RoutingDecision parseDecision(String decision) {
		if (decision == null) {
			return RoutingDecision.COMMON_QUEUE;
		}
		try {
			return RoutingDecision.valueOf(decision);
		} catch (IllegalArgumentException e) {
			log.warn("알 수 없는 AI 라우팅 결정 값: {}", decision);
			return RoutingDecision.COMMON_QUEUE;
		}
	}

	private String buildModelVersion(String model, String provider) {
		if (model == null && provider == null) {
			return null;
		}
		return model + "@" + provider;
	}
}
