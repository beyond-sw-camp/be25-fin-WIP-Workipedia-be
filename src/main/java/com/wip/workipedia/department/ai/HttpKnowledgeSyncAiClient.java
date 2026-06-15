package com.wip.workipedia.department.ai;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class HttpKnowledgeSyncAiClient implements KnowledgeSyncAiClient {

	private final RestClient routingAiRestClient;

	public HttpKnowledgeSyncAiClient(@Qualifier("routingAiRestClient") RestClient routingAiRestClient) {
		this.routingAiRestClient = routingAiRestClient;
	}

	@Override
	public void sync(KnowledgeSyncRequest request) {
		try {
			routingAiRestClient.post()
				.uri("/api/v1/knowledge/sync")
				.body(request)
				.retrieve()
				.toBodilessEntity();
		} catch (Exception e) {
			log.error("AI 지식 동기화 실패 (sourceId={}, sourceType={}): {}",
				request.sourceId(), request.sourceType(), e.getMessage());
			throw new CustomException(ErrorType.AI_SYNC_FAILED,
				"AI 지식 동기화에 실패했습니다. departmentId=" + request.departmentId());
		}
	}

	@Override
	public void delete(Long sourceId, String sourceType) {
		try {
			routingAiRestClient.delete()
				.uri("/api/v1/knowledge/{sourceId}?sourceType={sourceType}", sourceId, sourceType)
				.retrieve()
				.toBodilessEntity();
		} catch (Exception e) {
			log.error("AI 지식 삭제 실패 (sourceId={}, sourceType={}): {}", sourceId, sourceType, e.getMessage());
			throw new CustomException(ErrorType.AI_SYNC_FAILED,
				"AI 지식 삭제에 실패했습니다. sourceId=" + sourceId);
		}
	}
}
