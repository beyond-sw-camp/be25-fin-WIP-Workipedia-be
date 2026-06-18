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
		log.info("[AI라우팅] 호출 시작 title={}, sourceChatbotMessageId={}", prompt.title(), prompt.sourceChatbotMessageId());
		try {
			AiRoutingResponse response = routingAiRestClient.post()
				.uri("/api/v1/tickets/routing")
				.body(prompt)
				.retrieve()
				.body(AiRoutingResponse.class);

			if (response == null) {
				log.error("[AI라우팅] 응답이 null → 공통 큐로 폴백");
				return fallback.recommend(prompt);
			}

			log.info("[AI라우팅] 응답 수신 decision={}, 부서id={}, 부서명={}, confidence={}, 후보수={}, reasons={}",
				response.decision(), response.assignedDepartmentId(), response.assignedDepartmentName(),
				response.confidenceScore(),
				response.candidateDepartments() == null ? 0 : response.candidateDepartments().size(),
				response.reasons());
			return toRoutingResult(response);
		} catch (Exception e) {
			log.error("[AI라우팅] 호출 실패 → 공통 큐로 폴백: {}", e.getMessage(), e);
			return fallback.recommend(prompt);
		}
	}

	private RoutingResult toRoutingResult(AiRoutingResponse response) {
		RoutingDecision decision = parseDecision(response.decision());

		List<CandidateDepartmentResponse> candidates = response.candidateDepartments() == null ? List.of() :
			response.candidateDepartments().stream()
				.map(c -> new CandidateDepartmentResponse(c.departmentId(), c.departmentName(), c.confidenceScore()))
				.toList();

		boolean isCommonQueue = decision == RoutingDecision.COMMON_QUEUE;
		return new RoutingResult(
			isCommonQueue ? null : response.assignedDepartmentId(),
			isCommonQueue ? null : response.assignedDepartmentName(),
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
