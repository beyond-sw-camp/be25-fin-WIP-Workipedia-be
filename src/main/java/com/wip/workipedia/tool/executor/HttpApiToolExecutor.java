package com.wip.workipedia.tool.executor;

import com.wip.workipedia.tool.domain.AiTool;
import com.wip.workipedia.tool.domain.AuthType;
import com.wip.workipedia.tool.exception.ToolExecutionException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class HttpApiToolExecutor {

	private final ToolRestClientFactory restClientFactory;
	private final Environment environment;
	private final SsrfGuard ssrfGuard;

	public ToolExecutionResult execute(AiTool tool, Map<String, Object> parameters) {
		if (!ssrfGuard.isSafe(tool.getEndpointUrl())) {
			throw new ToolExecutionException("UNSAFE_ENDPOINT", "내부망/루프백 주소로는 Tool을 실행할 수 없습니다.");
		}

		RestClient client = restClientFactory.create(tool.getTimeoutMs());
		HttpMethod method;
		try {
			method = HttpMethod.valueOf(tool.getHttpMethod());
		} catch (IllegalArgumentException e) {
			throw new ToolExecutionException("INVALID_HTTP_METHOD", "지원하지 않는 HTTP method입니다: " + tool.getHttpMethod());
		}

		try {
			Object body;
			if (method == HttpMethod.GET) {
				body = client.get()
					.uri(buildGetUri(tool.getEndpointUrl(), parameters))
					.headers(headers -> applyAuth(headers, tool))
					.retrieve()
					.body(Object.class);
			} else {
				body = client.method(method)
					.uri(URI.create(tool.getEndpointUrl()))
					.headers(headers -> applyAuth(headers, tool))
					.body(parameters)
					.retrieve()
					.body(Object.class);
			}
			return buildResult(tool, body);
		} catch (RestClientException e) {
			throw new ToolExecutionException("EXTERNAL_API_ERROR", "외부 API 호출에 실패했습니다.");
		}
	}

	private URI buildGetUri(String endpointUrl, Map<String, Object> parameters) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(endpointUrl);
		parameters.forEach(builder::queryParam);
		return builder.build().encode().toUri();
	}

	private void applyAuth(HttpHeaders headers, AiTool tool) {
		AuthType authType = tool.getAuthType();
		if (authType == AuthType.NONE) {
			return;
		}

		String credential = environment.getProperty(tool.getCredentialRef());
		if (credential == null || credential.isBlank()) {
			throw new ToolExecutionException("CREDENTIAL_NOT_CONFIGURED", "Tool credential이 설정되지 않았습니다.");
		}

		switch (authType) {
			case API_KEY -> headers.set("X-API-Key", credential);
			case BEARER_TOKEN -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + credential);
			default -> throw new ToolExecutionException("AUTH_TYPE_NOT_SUPPORTED", "지원하지 않는 인증 방식입니다.");
		}
	}

	private ToolExecutionResult buildResult(AiTool tool, Object body) {
		if (body instanceof List<?> list) {
			List<?> truncated = list.size() > tool.getMaxResultCount()
				? list.subList(0, tool.getMaxResultCount())
				: list;
			return new ToolExecutionResult(truncated, truncated.size());
		}
		return new ToolExecutionResult(body, body != null ? 1 : 0);
	}
}
