package com.wip.workipedia.tool.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiToolTest {

	@Test
	void createHttpApiTool_초기상태는_APPROVED_비활성() {
		AiTool tool = AiTool.createHttpApiTool(
			"직원정보조회", "직원 정보를 조회합니다.",
			"https://hr.example.com/api/employees", "GET",
			"{\"properties\":{\"employeeId\":{\"type\":\"string\",\"required\":true}}}",
			null, AuthType.API_KEY, "TOOL_HR_API_KEY", 5000, 100, 1L
		);

		assertThat(tool.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
		assertThat(tool.isActive()).isFalse();
		assertThat(tool.isExecutable()).isFalse();
	}

	@Test
	void isExecutable_승인은_되어있어도_활성화해야_true() {
		AiTool tool = AiTool.createHttpApiTool(
			"직원정보조회", "직원 정보를 조회합니다.",
			"https://hr.example.com/api/employees", "GET",
			"{\"properties\":{}}", null, AuthType.NONE, null, 5000, 100, 1L
		);

		assertThat(tool.isExecutable()).isFalse();

		tool.changeActive(true, 1L);
		assertThat(tool.isExecutable()).isTrue();
	}
}
