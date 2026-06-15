package com.wip.workipedia.department.ai;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Primary
@Component
public class HttpDepartmentRoutingPromptEditor implements DepartmentRoutingPromptEditor {

	private final RestClient routingAiRestClient;

	public HttpDepartmentRoutingPromptEditor(@Qualifier("routingAiRestClient") RestClient routingAiRestClient) {
		this.routingAiRestClient = routingAiRestClient;
	}

	@Override
	public List<RoutingPromptEditResult> edit(List<RoutingPromptEditTarget> targets, String instruction) {
		AiRoutingPromptResponse response;
		try {
			response = routingAiRestClient.post()
				.uri("/api/v1/department/routing-prompt")
				.body(new AiRoutingPromptRequest(instruction, targets))
				.retrieve()
				.body(AiRoutingPromptResponse.class);
		} catch (Exception e) {
			log.error("AI R&R 편집 호출 실패: {}", e.getMessage());
			throw new CustomException(ErrorType.AI_SYNC_FAILED, "AI R&R 편집에 실패했습니다.");
		}

		if (response == null || response.results() == null) {
			throw new CustomException(ErrorType.AI_SYNC_FAILED, "AI R&R 응답이 비어 있습니다.");
		}

		return response.results().stream()
			.map(r -> new RoutingPromptEditResult(r.departmentId(), r.routingPrompt()))
			.toList();
	}
}
