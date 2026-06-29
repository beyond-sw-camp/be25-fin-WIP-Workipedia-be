package com.wip.workipedia.departmentsync.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DepartmentSyncControllerTest {

	@Autowired MockMvc mockMvc;

	@Test
	@WithMockUser(roles = "SYSTEM_ADMIN")
	void preview_엔드포인트는_diff를_반환한다() throws Exception {
		String body = """
			{"sourceSystem":"CTRL_ERP","items":[{"externalId":"C-1","departmentName":"테스트팀","useYn":"Y"}]}
			""";
		mockMvc.perform(post("/api/v1/admin/departments/sync/preview")
				.contentType(MediaType.APPLICATION_JSON).content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.rows").isArray());
	}
}
