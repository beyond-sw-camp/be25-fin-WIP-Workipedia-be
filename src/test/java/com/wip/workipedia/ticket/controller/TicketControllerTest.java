package com.wip.workipedia.ticket.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.assertj.core.api.Assertions.assertThat;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(
		statements = {
			"DELETE FROM tickets",
			"ALTER TABLE tickets AUTO_INCREMENT = 1",
			"DELETE FROM users WHERE user_id = 1",
			"DELETE FROM departments WHERE department_id = 1",
			"INSERT INTO departments (department_id, department_name, created_at, updated_at) VALUES (1, '테스트부서', NOW(), NOW())",
		"INSERT INTO users (user_id, department_id, employee_id, email, password, nickname, role, status, created_at, updated_at) VALUES (1, 1, '20260001', 'test@workipedia.local', 'password', '테스트사용자', 'USER', 'ACTIVE', NOW(), NOW())"
	},
	executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class TicketControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void createTicketMovesToCommonQueueBeforeAiRoutingIsConnected() throws Exception {
		mockMvc.perform(post("/api/v1/tickets")
				.with(authentication(auth(1L)))
				.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{
						  "sourceChatbotMessageId": null,
						  "type": "REQUEST",
						  "priority": "HIGH",
						  "title": "VPN 접속 오류 처리 요청",
						  "content": "VPN 접속 오류 처리를 요청합니다."
						}
			"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("COMMON_QUEUE"))
			.andExpect(jsonPath("$.priority").value("HIGH"))
			.andExpect(jsonPath("$.assignedDepartmentId").value(Matchers.nullValue()))
			.andExpect(jsonPath("$.assignedDepartmentName").value(Matchers.nullValue()))
			.andExpect(jsonPath("$.routingDecision").value("COMMON_QUEUE"));

		Long requesterId = jdbcTemplate.queryForObject(
				"SELECT requester_id FROM tickets WHERE ticket_id = 1",
				Long.class
		);
		assertThat(requesterId).isEqualTo(1L);
	}

	@Test
	void listDetailAndAssignTicket() throws Exception {
		mockMvc.perform(post("/api/v1/tickets")
				.with(authentication(auth(1L)))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "type": "REQUEST",
					  "title": "SSD 추가 장착 확인 요청",
					  "content": "SSD 추가 장착과 보안씰 처리를 확인받고 싶습니다."
					}
			"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("COMMON_QUEUE"))
			.andExpect(jsonPath("$.priority").value("MEDIUM"));

		mockMvc.perform(get("/api/v1/tickets")
				.with(authentication(auth(1L))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(1))
			.andExpect(jsonPath("$.pageInfo.page").value(1))
			.andExpect(jsonPath("$.pageInfo.size").value(10))
			.andExpect(jsonPath("$.pageInfo.totalElements").value(1));

		mockMvc.perform(get("/api/v1/tickets")
				.with(authentication(auth(1L)))
				.param("status", "COMMON_QUEUE"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(1))
			.andExpect(jsonPath("$.content[0].status").value("COMMON_QUEUE"))
			.andExpect(jsonPath("$.content[0].priority").value("MEDIUM"));

		mockMvc.perform(get("/api/v1/tickets")
				.with(authentication(auth(1L)))
				.param("status", "ASSIGNED"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(0));

		mockMvc.perform(get("/api/v1/tickets")
				.with(authentication(auth(1L)))
				.param("status", "COMMON_QUEUE")
				.param("departmentId", "1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(0));

		mockMvc.perform(get("/api/v1/tickets/1")
				.with(authentication(auth(1L))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.ticketId").value(1));

		mockMvc.perform(patch("/api/v1/tickets/1/assignee")
				.with(authentication(auth(1L)))
				.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{
						  "assigneeId": 1,
						  "memo": "VPN 계정 확인 후 처리 부탁드립니다."
						}
				"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("IN_PROGRESS"))
				.andExpect(jsonPath("$.priority").value("MEDIUM"))
				.andExpect(jsonPath("$.assigneeId").value(1));
	}

	private UsernamePasswordAuthenticationToken auth(Long userId) {
		return new UsernamePasswordAuthenticationToken(userId, null, List.of());
	}
}
