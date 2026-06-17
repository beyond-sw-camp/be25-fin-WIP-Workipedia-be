package com.wip.workipedia.tool.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.tool.domain.AiTool;
import com.wip.workipedia.tool.domain.ApprovalStatus;
import com.wip.workipedia.tool.domain.AuthType;
import com.wip.workipedia.tool.dto.ToolExecuteResponse;
import com.wip.workipedia.tool.exception.ToolExecutionException;
import com.wip.workipedia.tool.executor.DbQueryToolExecutor;
import com.wip.workipedia.tool.executor.HttpApiToolExecutor;
import com.wip.workipedia.tool.executor.ToolExecutionResult;
import com.wip.workipedia.tool.repository.AiToolRepository;
import com.wip.workipedia.tool.repository.ToolExecutionLogRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ToolExecutionServiceTest {

	private static final String SCHEMA = "{\"properties\":{\"employeeId\":{\"type\":\"string\",\"required\":true}}}";

	@Mock AiToolRepository aiToolRepository;
	@Mock ToolExecutionLogRepository toolExecutionLogRepository;
	@Mock HttpApiToolExecutor httpApiToolExecutor;
	@Mock DbQueryToolExecutor dbQueryToolExecutor;
	@Spy ParameterSchemaValidator parameterSchemaValidator = new ParameterSchemaValidator(new ObjectMapper());
	@Spy ObjectMapper objectMapper = new ObjectMapper();

	@InjectMocks ToolExecutionService toolExecutionService;

	private AiTool executableTool() {
		AiTool tool = AiTool.createHttpApiTool(
			"직원정보조회", "직원 정보를 조회합니다.",
			"https://hr.example.com/api/employees", "GET", SCHEMA, null,
			AuthType.NONE, null, 5000, 100, 1L
		);
		tool.changeApprovalStatus(ApprovalStatus.APPROVED, 1L);
		tool.changeActive(true, 1L);
		return tool;
	}

	@Test
	void execute_정상_실행시_성공_응답과_감사로그_기록() {
		AiTool tool = executableTool();
		given(aiToolRepository.findByAiToolIdAndIsDeleted(1L, "N")).willReturn(Optional.of(tool));
		given(httpApiToolExecutor.execute(tool, Map.of("employeeId", "E001")))
			.willReturn(new ToolExecutionResult(Map.of("name", "홍길동"), 1));

		ToolExecuteResponse response = toolExecutionService.execute("ai-server", 1L, Map.of("employeeId", "E001"));

		assertThat(response.errorCode()).isNull();
		assertThat(response.data()).isNotNull();
		verify(toolExecutionLogRepository).save(any());
	}

	@Test
	void execute_DB_QUERY이면_DbQueryToolExecutor를_호출() {
		AiTool tool = AiTool.createDbQueryTool(
			"휴가잔여일조회", "직원 휴가 잔여일을 조회합니다.",
			"workipediaReadonly",
			"SELECT name, remaining_days FROM employee_vacations WHERE employee_id = :employeeId LIMIT 1",
			SCHEMA, null, 3000, 10, 1L
		);
		tool.changeApprovalStatus(ApprovalStatus.APPROVED, 1L);
		tool.changeActive(true, 1L);
		given(aiToolRepository.findByAiToolIdAndIsDeleted(2L, "N")).willReturn(Optional.of(tool));
		given(dbQueryToolExecutor.execute(tool, Map.of("employeeId", "E001")))
			.willReturn(new ToolExecutionResult(java.util.List.of(Map.of("remainingDays", 3)), 1));

		ToolExecuteResponse response = toolExecutionService.execute("ai-server", 2L, Map.of("employeeId", "E001"));

		assertThat(response.errorCode()).isNull();
		verify(dbQueryToolExecutor).execute(tool, Map.of("employeeId", "E001"));
		verify(toolExecutionLogRepository).save(any());
	}

	@Test
	void execute_비활성_Tool은_AI_TOOL_NOT_EXECUTABLE_예외() {
		AiTool tool = AiTool.createHttpApiTool(
			"직원정보조회", "직원 정보를 조회합니다.",
			"https://hr.example.com/api/employees", "GET", SCHEMA, null,
			AuthType.NONE, null, 5000, 100, 1L
		);
		given(aiToolRepository.findByAiToolIdAndIsDeleted(1L, "N")).willReturn(Optional.of(tool));

		assertThatThrownBy(() -> toolExecutionService.execute("ai-server", 1L, Map.of("employeeId", "E001")))
			.isInstanceOf(CustomException.class);
		verify(toolExecutionLogRepository).save(any());
	}

	@Test
	void execute_스키마에_안맞는_파라미터는_AI_TOOL_PARAMETER_MISMATCH_예외() {
		AiTool tool = executableTool();
		given(aiToolRepository.findByAiToolIdAndIsDeleted(1L, "N")).willReturn(Optional.of(tool));

		assertThatThrownBy(() -> toolExecutionService.execute("ai-server", 1L, Map.of()))
			.isInstanceOf(CustomException.class);
		verify(toolExecutionLogRepository).save(any());
	}

	@Test
	void execute_외부API_실패시_data_null인_실패_응답을_200으로_반환() {
		AiTool tool = executableTool();
		given(aiToolRepository.findByAiToolIdAndIsDeleted(1L, "N")).willReturn(Optional.of(tool));
		given(httpApiToolExecutor.execute(tool, Map.of("employeeId", "E001")))
			.willThrow(new ToolExecutionException("EXTERNAL_API_ERROR", "외부 API 호출에 실패했습니다."));

		ToolExecuteResponse response = toolExecutionService.execute("ai-server", 1L, Map.of("employeeId", "E001"));

		assertThat(response.data()).isNull();
		assertThat(response.errorCode()).isEqualTo("EXTERNAL_API_ERROR");
		verify(toolExecutionLogRepository).save(any());
	}

	@Test
	void execute_삭제된_Tool은_AI_TOOL_NOT_FOUND_예외() {
		given(aiToolRepository.findByAiToolIdAndIsDeleted(1L, "N")).willReturn(Optional.empty());

		assertThatThrownBy(() -> toolExecutionService.execute("ai-server", 1L, Map.of("employeeId", "E001")))
			.isInstanceOf(CustomException.class);
	}

	@Test
	void findActiveTools_활성화되고_승인된_Tool만_반환() {
		given(aiToolRepository.findByIsActiveAndApprovalStatusAndIsDeleted("Y", ApprovalStatus.APPROVED, "N"))
			.willReturn(java.util.List.of(executableTool()));

		var result = toolExecutionService.findActiveTools();

		assertThat(result).hasSize(1);
	}
}
