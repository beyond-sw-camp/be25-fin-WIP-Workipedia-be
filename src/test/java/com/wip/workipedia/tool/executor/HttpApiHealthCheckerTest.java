package com.wip.workipedia.tool.executor;

import com.wip.workipedia.tool.domain.AiTool;
import com.wip.workipedia.tool.domain.AuthType;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class HttpApiHealthCheckerTest {

	private final RestClient.Builder builder = RestClient.builder();
	private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
	private final ToolRestClientFactory restClientFactory = timeoutMs -> builder.build();
	private final SsrfGuard ssrfGuard = endpointUrl -> true;
	private final Environment environment = mock(Environment.class);
	private final HttpApiHealthChecker checker = new HttpApiHealthChecker(restClientFactory, ssrfGuard, environment);

	private AiTool httpApiTool(String httpMethod) {
		return AiTool.createHttpApiTool(
			"직원정보조회", "설명",
			"https://hr.example.com/api/employees", httpMethod,
			"{\"properties\":{}}", null, AuthType.NONE, null, 5000, 100, 1L
		);
	}

	@Test
	void check_2xx_응답이면_성공() {
		server.expect(MockRestRequestMatchers.requestTo("https://hr.example.com/api/employees"))
			.andRespond(MockRestResponseCreators.withSuccess());

		HealthCheckResult result = checker.check(httpApiTool("GET"));

		assertThat(result.success()).isTrue();
	}

	@Test
	void check_4xx_응답이면_실패() {
		server.expect(MockRestRequestMatchers.requestTo("https://hr.example.com/api/employees"))
			.andRespond(MockRestResponseCreators.withStatus(HttpStatus.NOT_FOUND));

		HealthCheckResult result = checker.check(httpApiTool("GET"));

		assertThat(result.success()).isFalse();
	}

	@Test
	void check_POST_Tool은_빈_JSON_body로_요청() {
		server.expect(MockRestRequestMatchers.requestTo("https://hr.example.com/api/employees"))
			.andExpect(MockRestRequestMatchers.content().json("{}"))
			.andRespond(MockRestResponseCreators.withSuccess());

		HealthCheckResult result = checker.check(httpApiTool("POST"));

		assertThat(result.success()).isTrue();
	}

	@Test
	void check_안전하지않은_endpoint면_실패() {
		SsrfGuard unsafeGuard = endpointUrl -> false;
		HttpApiHealthChecker uncheckedChecker = new HttpApiHealthChecker(restClientFactory, unsafeGuard, environment);

		HealthCheckResult result = uncheckedChecker.check(httpApiTool("GET"));

		assertThat(result.success()).isFalse();
	}
}
