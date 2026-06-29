package com.wip.workipedia.tool.executor;

import com.wip.workipedia.tool.domain.AiTool;
import com.wip.workipedia.tool.domain.AuthType;
import com.wip.workipedia.tool.domain.SideEffectType;
import com.wip.workipedia.tool.exception.ToolExecutionException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpApiToolExecutorTest {

	private final RestClient.Builder builder = RestClient.builder();
	private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
	private final ToolRestClientFactory restClientFactory = timeoutMs -> builder.build();
	private final Environment environment = mock(Environment.class);
	private final SsrfGuard ssrfGuard = endpointUrl -> true;
	private final HttpApiToolExecutor executor = new HttpApiToolExecutor(restClientFactory, environment, ssrfGuard);

	@Test
	void execute_GET_요청은_쿼리파라미터로_전달하고_응답을_그대로_반환() {
		AiTool tool = AiTool.createHttpApiTool(
			"직원정보조회", "직원 정보를 조회합니다.",
			"https://hr.example.com/api/employees", "GET",
			"{\"properties\":{\"employeeId\":{\"type\":\"string\",\"required\":true}}}",
			null, SideEffectType.READ_ONLY, AuthType.NONE, null, 5000, 100, 1L
		);

		server.expect(MockRestRequestMatchers.requestTo("https://hr.example.com/api/employees?employeeId=E001"))
			.andRespond(MockRestResponseCreators.withSuccess(
				"{\"name\":\"홍길동\"}", MediaType.APPLICATION_JSON
			));

		ToolExecutionResult result = executor.execute(tool, Map.of("employeeId", "E001"));

		assertThat(result.resultCount()).isEqualTo(1);
		server.verify();
	}

	@Test
	void execute_리스트응답은_maxResultCount로_잘림() {
		AiTool tool = AiTool.createHttpApiTool(
			"직원목록조회", "직원 목록을 조회합니다.",
			"https://hr.example.com/api/employees/list", "GET",
			"{\"properties\":{}}", null, SideEffectType.READ_ONLY, AuthType.NONE, null, 5000, 1, 1L
		);

		server.expect(MockRestRequestMatchers.requestTo("https://hr.example.com/api/employees/list"))
			.andRespond(MockRestResponseCreators.withSuccess(
				"[{\"name\":\"a\"},{\"name\":\"b\"}]", MediaType.APPLICATION_JSON
			));

		ToolExecutionResult result = executor.execute(tool, Map.of());

		assertThat(result.resultCount()).isEqualTo(1);
	}

	@Test
	void execute_API_KEY_인증은_헤더에_credential을_담아_전달() {
		AiTool tool = AiTool.createHttpApiTool(
			"직원정보조회", "직원 정보를 조회합니다.",
			"https://hr.example.com/api/employees", "GET",
			"{\"properties\":{}}", null, SideEffectType.READ_ONLY, AuthType.API_KEY, "TOOL_HR_API_KEY", 5000, 100, 1L
		);
		when(environment.getProperty("TOOL_HR_API_KEY")).thenReturn("secret-key");

		server.expect(MockRestRequestMatchers.requestTo("https://hr.example.com/api/employees"))
			.andExpect(MockRestRequestMatchers.header("X-API-Key", "secret-key"))
			.andRespond(MockRestResponseCreators.withSuccess("{}", MediaType.APPLICATION_JSON));

		executor.execute(tool, Map.of());
	}

	@Test
	void execute_credential이_없으면_ToolExecutionException() {
		AiTool tool = AiTool.createHttpApiTool(
			"직원정보조회", "직원 정보를 조회합니다.",
			"https://hr.example.com/api/employees", "GET",
			"{\"properties\":{}}", null, SideEffectType.READ_ONLY, AuthType.API_KEY, "TOOL_HR_API_KEY", 5000, 100, 1L
		);
		when(environment.getProperty("TOOL_HR_API_KEY")).thenReturn(null);

		assertThatThrownBy(() -> executor.execute(tool, Map.of()))
			.isInstanceOf(ToolExecutionException.class)
			.satisfies(e -> assertThat(((ToolExecutionException) e).getErrorCode()).isEqualTo("CREDENTIAL_NOT_CONFIGURED"));
	}

	@Test
	void execute_외부API_오류시_ToolExecutionException() {
		AiTool tool = AiTool.createHttpApiTool(
			"직원정보조회", "직원 정보를 조회합니다.",
			"https://hr.example.com/api/employees", "GET",
			"{\"properties\":{}}", null, SideEffectType.READ_ONLY, AuthType.NONE, null, 5000, 100, 1L
		);

		server.expect(MockRestRequestMatchers.requestTo("https://hr.example.com/api/employees"))
			.andRespond(MockRestResponseCreators.withServerError());

		assertThatThrownBy(() -> executor.execute(tool, Map.of()))
			.isInstanceOf(ToolExecutionException.class)
			.satisfies(e -> assertThat(((ToolExecutionException) e).getErrorCode()).isEqualTo("EXTERNAL_API_ERROR"));
	}

	@Test
	void execute_안전하지않은_endpoint면_ToolExecutionException() {
		SsrfGuard unsafeGuard = endpointUrl -> false;
		HttpApiToolExecutor unsafeExecutor = new HttpApiToolExecutor(restClientFactory, environment, unsafeGuard);
		AiTool tool = AiTool.createHttpApiTool(
			"내부망조회", "설명",
			"https://192.168.1.1/api", "GET",
			"{\"properties\":{}}", null, SideEffectType.READ_ONLY, AuthType.NONE, null, 5000, 100, 1L
		);

		assertThatThrownBy(() -> unsafeExecutor.execute(tool, Map.of()))
			.isInstanceOf(ToolExecutionException.class)
			.satisfies(e -> assertThat(((ToolExecutionException) e).getErrorCode()).isEqualTo("UNSAFE_ENDPOINT"));
	}
}
