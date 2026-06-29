package com.wip.workipedia.admin.aitool.controller;

import com.wip.workipedia.admin.aitool.dto.AiToolResponse;
import com.wip.workipedia.admin.aitool.dto.HealthCheckResponse;
import com.wip.workipedia.admin.aitool.service.AdminAiToolService;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.common.security.InternalApiKeyFilter;
import com.wip.workipedia.common.security.JwtFilter;
import com.wip.workipedia.common.security.JwtProvider;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
	value = AdminAiToolController.class,
	excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class},
	excludeFilters = @ComponentScan.Filter(
		type = FilterType.ASSIGNABLE_TYPE,
		classes = {JwtFilter.class, JwtProvider.class, InternalApiKeyFilter.class}
	)
)
class AdminAiToolControllerTest {

	@Autowired MockMvc mockMvc;
	@MockitoBean AdminAiToolService adminAiToolService;

	@Test
	void findAll_목록_조회() throws Exception {
		AiToolResponse response = new AiToolResponse(
			1L, "직원정보조회", "설명", "HTTP_API",
			"https://hr.example.com", "GET", null, null,
			"{}", null, "UNRESTRICTED", null,
			"NONE", null, 5000, 100,
			"APPROVED", false, LocalDateTime.now(), LocalDateTime.now()
		);
		given(adminAiToolService.findAll(any())).willReturn(
			new PageResponse<>(List.of(response), new PageResponse.PageInfo(1, 10, 1, 1, false, false))
		);

		mockMvc.perform(get("/api/v1/admin/ai-tools").with(authentication(auth(1L))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].name").value("직원정보조회"));
	}

	@Test
	void create_등록_요청() throws Exception {
		AiToolResponse response = new AiToolResponse(
			1L, "직원정보조회", "설명", "HTTP_API",
			"https://hr.example.com", "GET", null, null,
			"{}", null, "UNRESTRICTED", null,
			"NONE", null, 5000, 100,
			"APPROVED", false, LocalDateTime.now(), LocalDateTime.now()
		);
		given(adminAiToolService.create(nullable(Long.class), any())).willReturn(response);

		mockMvc.perform(post("/api/v1/admin/ai-tools")
				.with(authentication(auth(1L)))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "name": "직원정보조회",
						  "description": "직원 정보를 조회합니다.",
						  "toolType": "HTTP_API",
						  "endpointUrl": "https://hr.example.com",
						  "httpMethod": "GET",
						  "parametersSchema": "{\\"properties\\":{}}",
						  "authType": "NONE",
						  "timeoutMs": 5000,
						  "maxResultCount": 100
						}
						"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.name").value("직원정보조회"));
	}

	@Test
	void healthCheck_연결확인_요청() throws Exception {
		given(adminAiToolService.healthCheck(1L))
			.willReturn(new HealthCheckResponse(true, "HTTP_API", 120, null));

		mockMvc.perform(post("/api/v1/admin/ai-tools/1/health-check").with(authentication(auth(1L))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));
	}

	private UsernamePasswordAuthenticationToken auth(Long userId) {
		return new UsernamePasswordAuthenticationToken(userId, null, List.of());
	}
}
