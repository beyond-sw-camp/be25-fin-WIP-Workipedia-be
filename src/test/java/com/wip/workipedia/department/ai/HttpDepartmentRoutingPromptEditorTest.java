package com.wip.workipedia.department.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.wip.workipedia.common.exception.CustomException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class HttpDepartmentRoutingPromptEditorTest {

	private MockRestServiceServer mockServer;
	private HttpDepartmentRoutingPromptEditor editor;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder().baseUrl("http://ai-server");
		mockServer = MockRestServiceServer.bindTo(builder).build();
		editor = new HttpDepartmentRoutingPromptEditor(builder.build());
	}

	@Test
	void AI_응답_정상이면_결과_반환() {
		mockServer.expect(requestTo("http://ai-server/api/v1/department/routing-prompt"))
			.andRespond(withSuccess("""
				{
				  "results": [
				    {"departmentId": 2, "routingPrompt": "개발 2팀은 RAG를 담당한다."}
				  ]
				}
				""", MediaType.APPLICATION_JSON));

		List<RoutingPromptEditTarget> targets = List.of(
			new RoutingPromptEditTarget(2L, "개발 2팀", "개발 2팀은 검색을 담당한다.")
		);
		List<RoutingPromptEditResult> results = editor.edit(targets, "개발 2팀에 RAG 추가");

		assertThat(results).hasSize(1);
		assertThat(results.get(0).departmentId()).isEqualTo(2L);
		assertThat(results.get(0).routingPrompt()).isEqualTo("개발 2팀은 RAG를 담당한다.");
	}

	@Test
	void AI_서버_오류_시_예외_발생() {
		mockServer.expect(requestTo("http://ai-server/api/v1/department/routing-prompt"))
			.andRespond(withServerError());

		assertThatThrownBy(() -> editor.edit(
			List.of(new RoutingPromptEditTarget(2L, "개발 2팀", "현재 프롬프트")),
			"테스트 지시"
		)).isInstanceOf(CustomException.class);
	}
}
