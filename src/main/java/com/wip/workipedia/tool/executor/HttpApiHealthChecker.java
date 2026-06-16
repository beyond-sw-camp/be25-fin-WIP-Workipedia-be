package com.wip.workipedia.tool.executor;

import com.wip.workipedia.tool.domain.AiTool;
import com.wip.workipedia.tool.domain.AuthType;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class HttpApiHealthChecker {

	private static final Set<HttpMethod> BODY_METHODS = Set.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH);

	private final ToolRestClientFactory restClientFactory;
	private final SsrfGuard ssrfGuard;
	private final Environment environment;

	public HealthCheckResult check(AiTool tool) {
		if (!ssrfGuard.isSafe(tool.getEndpointUrl())) {
			return HealthCheckResult.failure("내부망/루프백 주소로는 health-check를 수행할 수 없습니다.");
		}

		HttpMethod method = HttpMethod.valueOf(tool.getHttpMethod());
		RestClient client = restClientFactory.create(tool.getTimeoutMs());
		long startedAt = System.currentTimeMillis();

		try {
			int statusCode;
			if (BODY_METHODS.contains(method)) {
				statusCode = client.method(method)
					.uri(URI.create(tool.getEndpointUrl()))
					.headers(headers -> applyAuth(headers, tool))
					.body(Map.of())
					.exchange((request, response) -> response.getStatusCode().value());
			} else {
				statusCode = client.method(method)
					.uri(URI.create(tool.getEndpointUrl()))
					.headers(headers -> applyAuth(headers, tool))
					.exchange((request, response) -> response.getStatusCode().value());
			}

			long latencyMs = System.currentTimeMillis() - startedAt;
			if (statusCode >= 200 && statusCode < 300) {
				return HealthCheckResult.success(latencyMs);
			}
			return HealthCheckResult.failure(latencyMs, "응답 코드: " + statusCode);
		} catch (Exception e) {
			long latencyMs = System.currentTimeMillis() - startedAt;
			return HealthCheckResult.failure(latencyMs, "외부 API 호출에 실패했습니다.");
		}
	}

	private void applyAuth(HttpHeaders headers, AiTool tool) {
		AuthType authType = tool.getAuthType();
		if (authType == AuthType.NONE) {
			return;
		}

		String credential = environment.getProperty(tool.getCredentialRef());
		if (credential == null || credential.isBlank()) {
			return;
		}

		switch (authType) {
			case API_KEY -> headers.set("X-API-Key", credential);
			case BEARER_TOKEN -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + credential);
			default -> { }
		}
	}
}
