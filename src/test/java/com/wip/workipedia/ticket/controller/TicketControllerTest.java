package com.wip.workipedia.ticket.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(
		statements = {
			"DELETE FROM tickets",
			"ALTER TABLE tickets AUTO_INCREMENT = 1",
			"DELETE FROM users WHERE user_id = 1",
			"DELETE FROM departments WHERE department_id = 1",
			"INSERT INTO departments (department_id, name, code, created_at, updated_at) VALUES (1, '테스트부서', 'TEST', NOW(), NOW())",
		"INSERT INTO users (user_id, department_id, employee_id, email, password, nickname, role, status, created_at, updated_at) VALUES (1, 1, '20260001', 'test@workipedia.local', 'password', '테스트사용자', 'USER', 'ACTIVE', NOW(), NOW())"
	},
	executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class TicketControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Test
	void createTicketMovesToCommonQueueBeforeAiRoutingIsConnected() throws Exception {
		mockMvc.perform(post("/api/v1/tickets")
				.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{
						  "questionId": null,
						  "sourceChatbotMessageId": null,
						  "type": "REQUEST",
						  "categoryId": null,
						  "title": "VPN 접속 오류 처리 요청",
						  "content": "VPN 접속 오류 처리를 요청합니다."
						}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data.status").value("COMMON_QUEUE"))
			.andExpect(jsonPath("$.data.assignedDepartmentId").doesNotExist())
			.andExpect(jsonPath("$.data.assignedDepartmentName").doesNotExist())
			.andExpect(jsonPath("$.data.routingDecision").value("COMMON_QUEUE"));
	}

	@Test
	void listDetailAndAssignTicket() throws Exception {
		mockMvc.perform(post("/api/v1/tickets")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "type": "REQUEST",
					  "title": "SSD 추가 장착 확인 요청",
					  "content": "SSD 추가 장착과 보안씰 처리를 확인받고 싶습니다."
					}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data.status").value("COMMON_QUEUE"));

		mockMvc.perform(get("/api/v1/tickets"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").isNotEmpty());

		mockMvc.perform(get("/api/v1/tickets/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.ticketId").value(1));

		mockMvc.perform(patch("/api/v1/tickets/1/assignee")
				.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{
						  "assigneeId": 1,
						  "memo": "VPN 계정 확인 후 처리 부탁드립니다."
						}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
				.andExpect(jsonPath("$.data.assigneeId").value(1));
	}
}
