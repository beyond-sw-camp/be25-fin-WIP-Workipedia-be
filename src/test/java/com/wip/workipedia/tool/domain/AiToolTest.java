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
			null, SideEffectType.READ_ONLY, AuthType.API_KEY, "TOOL_HR_API_KEY", 5000, 100, 1L
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
			"{\"properties\":{}}", null, SideEffectType.READ_ONLY, AuthType.NONE, null, 5000, 100, 1L
		);

		assertThat(tool.isExecutable()).isFalse();

		tool.changeActive(true, 1L);
		assertThat(tool.isExecutable()).isTrue();
	}

	@Test
	void isAiExecutable_MUTATING_Tool은_활성화되어도_false() {
		AiTool tool = AiTool.createHttpApiTool(
			"휴가신청", "휴가를 신청합니다.",
			"https://hr.example.com/api/vacations", "POST",
			"{\"properties\":{}}", null, SideEffectType.MUTATING,
			AuthType.NONE, null, 5000, 100, 1L
		);
		tool.changeActive(true, 1L);

		assertThat(tool.isExecutable()).isTrue();
		assertThat(tool.isAiExecutable()).isFalse();
	}
}
