package com.wip.workipedia.tool.controller;

import com.wip.workipedia.common.security.InternalApiKeyFilter;
import com.wip.workipedia.common.security.JwtFilter;
import com.wip.workipedia.common.security.JwtProvider;
import com.wip.workipedia.tool.dto.ActiveAiToolResponse;
import com.wip.workipedia.tool.dto.ToolExecuteResponse;
import com.wip.workipedia.tool.service.ToolExecutionService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
	value = InternalAiToolController.class,
	excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class},
	excludeFilters = @ComponentScan.Filter(
		type = FilterType.ASSIGNABLE_TYPE,
		classes = {JwtFilter.class, JwtProvider.class, InternalApiKeyFilter.class}
	)
)
class InternalAiToolControllerTest {

	@Autowired MockMvc mockMvc;
	@MockitoBean ToolExecutionService toolExecutionService;

	@Test
	void getActiveTools_활성_Tool_목록_반환() throws Exception {
		given(toolExecutionService.findActiveTools())
			.willReturn(List.of(new ActiveAiToolResponse(1L, "HTTP_API", "직원정보조회", "설명", "{}", "UNRESTRICTED", null)));

		mockMvc.perform(get("/api/v1/internal/ai-tools/active"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].name").value("직원정보조회"));
	}

	@Test
	void execute_파라미터를_전달해서_실행_결과_반환() throws Exception {
		given(toolExecutionService.execute(eq("ai-server"), eq(1L), any(), any()))
			.willReturn(ToolExecuteResponse.success(Map.of("name", "홍길동")));

		mockMvc.perform(post("/api/v1/internal/ai-tools/1/execute")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"parameters": {"employeeId": "E001"}}
						"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.errorCode").doesNotExist());

		verify(toolExecutionService).execute(eq("ai-server"), eq(1L), any(), any());
	}
}
