package com.wip.workipedia.admin.aitool.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.admin.aitool.dto.AiToolCreateRequest;
import com.wip.workipedia.admin.aitool.dto.AiToolResponse;
import com.wip.workipedia.admin.aitool.dto.AiToolUpdateRequest;
import com.wip.workipedia.admin.aitool.dto.HealthCheckResponse;
import com.wip.workipedia.admin.domain.AdminLog;
import com.wip.workipedia.admin.repository.AdminLogRepository;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.tool.domain.AiTool;
import com.wip.workipedia.tool.domain.ApprovalStatus;
import com.wip.workipedia.tool.domain.AuthType;
import com.wip.workipedia.tool.domain.SideEffectType;
import com.wip.workipedia.tool.executor.DbQueryHealthChecker;
import com.wip.workipedia.tool.executor.HealthCheckResult;
import com.wip.workipedia.tool.executor.HttpApiHealthChecker;
import com.wip.workipedia.tool.executor.SsrfGuard;
import com.wip.workipedia.tool.repository.AiToolRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AdminAiToolServiceTest {

	@Mock AiToolRepository aiToolRepository;
	@Mock AdminLogRepository adminLogRepository;
	@Spy ObjectMapper objectMapper = new ObjectMapper();
	@Mock HttpApiHealthChecker httpApiHealthChecker;
	@Mock DbQueryHealthChecker dbQueryHealthChecker;
	@Mock RestClient routingAiRestClient;

	private boolean ssrfSafe = true;
	private final SsrfGuard ssrfGuard = endpointUrl -> ssrfSafe;

	private AdminAiToolService adminAiToolService;

	@BeforeEach
	void setUp() {
		adminAiToolService = new AdminAiToolService(
			aiToolRepository, adminLogRepository, objectMapper, ssrfGuard, httpApiHealthChecker, dbQueryHealthChecker,
			routingAiRestClient
		);
	}

	private AiToolCreateRequest createRequest() {
		return new AiToolCreateRequest(
			"직원정보조회", "직원 정보를 조회합니다.",
			"HTTP_API", "READ_ONLY", "https://hr.example.com/api/employees", "GET",
			null, null,
			"{\"properties\":{\"employeeId\":{\"type\":\"string\",\"required\":true}}}",
			null, "UNRESTRICTED", null, "API_KEY", "TOOL_HR_API_KEY", 5000, 100
		);
	}

	@Test
	void create_HTTP_API_등록_성공시_AdminLog_기록() {
		AiToolCreateRequest request = createRequest();

		AiToolResponse response = adminAiToolService.create(1L, request);

		assertThat(response.name()).isEqualTo("직원정보조회");
		assertThat(response.sideEffectType()).isEqualTo("READ_ONLY");
		assertThat(response.approvalStatus()).isEqualTo("APPROVED");
		assertThat(response.active()).isFalse();
		verify(aiToolRepository).save(any(AiTool.class));
		verify(adminLogRepository).save(any(AdminLog.class));
	}

	@Test
	void create_DB_QUERY_등록_성공시_AdminLog_기록() {
		AiToolCreateRequest request = new AiToolCreateRequest(
			"휴가잔여일조회", "설명", "DB_QUERY", "READ_ONLY", null, null,
			"workipediaReadonly",
			"SELECT name, remaining_days FROM employee_vacations WHERE employee_id = :employeeId LIMIT 1",
			"{\"properties\":{}}", null, "UNRESTRICTED", null, "NONE", null, 5000, 100
		);

		AiToolResponse response = adminAiToolService.create(1L, request);

		assertThat(response.toolType()).isEqualTo("DB_QUERY");
		assertThat(response.approvalStatus()).isEqualTo("APPROVED");
		verify(aiToolRepository).save(any(AiTool.class));
		verify(adminLogRepository).save(any(AdminLog.class));
	}

	@Test
	void create_MUTATING_HTTP_API_등록_성공() {
		AiToolCreateRequest request = new AiToolCreateRequest(
			"휴가신청", "설명", "HTTP_API", "MUTATING",
			"https://hr.example.com/api/vacations", "POST", null, null,
			"{\"properties\":{}}", null, "UNRESTRICTED", null, "NONE", null, 5000, 100
		);

		AiToolResponse response = adminAiToolService.create(1L, request);

		assertThat(response.sideEffectType()).isEqualTo("MUTATING");
	}

	@Test
	void create_DB_QUERY를_MUTATING으로_등록하면_거부() {
		AiToolCreateRequest request = new AiToolCreateRequest(
			"휴가잔여일조회", "설명", "DB_QUERY", "MUTATING", null, null,
			"workipediaReadonly", "SELECT remaining_days FROM employee_vacations LIMIT 1",
			"{\"properties\":{}}", null, "UNRESTRICTED", null, "NONE", null, 5000, 100
		);

		assertThatThrownBy(() -> adminAiToolService.create(1L, request))
			.isInstanceOf(CustomException.class);
	}

	@Test
	void create_DB_QUERY인데_datasourceKey가_없으면_거부() {
		AiToolCreateRequest request = new AiToolCreateRequest(
			"휴가잔여일조회", "설명", "DB_QUERY", "READ_ONLY", null, null,
			null, "SELECT name FROM employee_vacations LIMIT 1",
			"{\"properties\":{}}", null, "UNRESTRICTED", null, "NONE", null, 5000, 100
		);

		assertThatThrownBy(() -> adminAiToolService.create(1L, request))
			.isInstanceOf(CustomException.class);
	}

	@Test
	void create_DB_QUERY인데_queryTemplate이_없으면_거부() {
		AiToolCreateRequest request = new AiToolCreateRequest(
			"휴가잔여일조회", "설명", "DB_QUERY", "READ_ONLY", null, null,
			"workipediaReadonly", null,
			"{\"properties\":{}}", null, "UNRESTRICTED", null, "NONE", null, 5000, 100
		);

		assertThatThrownBy(() -> adminAiToolService.create(1L, request))
			.isInstanceOf(CustomException.class);
	}

	@Test
	void create_authType_API_KEY인데_credentialRef_없으면_거부() {
		AiToolCreateRequest request = new AiToolCreateRequest(
			"직원정보조회", "설명", "HTTP_API", "READ_ONLY", "https://hr.example.com", "GET",
			null, null,
			"{\"properties\":{}}", null, "UNRESTRICTED", null, "API_KEY", null, 5000, 100
		);

		assertThatThrownBy(() -> adminAiToolService.create(1L, request))
			.isInstanceOf(CustomException.class);
	}

	@Test
	void create_OAUTH2_인증타입은_M2범위에서_거부() {
		AiToolCreateRequest request = new AiToolCreateRequest(
			"직원정보조회", "설명", "HTTP_API", "READ_ONLY", "https://hr.example.com", "GET",
			null, null,
			"{\"properties\":{}}", null, "UNRESTRICTED", null, "OAUTH2", "ref", 5000, 100
		);

		assertThatThrownBy(() -> adminAiToolService.create(1L, request))
			.isInstanceOf(CustomException.class);
	}

	@Test
	void create_allowlist에_없는_endpoint_host는_AI_TOOL_UNSAFE_ENDPOINT_예외() {
		ssrfSafe = false;
		AiToolCreateRequest request = createRequest();

		assertThatThrownBy(() -> adminAiToolService.create(1L, request))
			.isInstanceOf(CustomException.class);
	}

	@Test
	void update_active를_true로_변경() {
		AiTool tool = AiTool.createHttpApiTool(
			"직원정보조회", "설명", "https://hr.example.com", "GET",
			"{\"properties\":{}}", null, SideEffectType.READ_ONLY, AuthType.NONE, null, 5000, 100, 1L
		);
		given(aiToolRepository.findById(1L)).willReturn(Optional.of(tool));

		AiToolResponse response = adminAiToolService.update(
			1L, 1L,
			new AiToolUpdateRequest(null, null, null, null, null, null, null, null, null, null, null, null, null, null, "APPROVED", true)
		);

		assertThat(response.approvalStatus()).isEqualTo("APPROVED");
		assertThat(response.active()).isTrue();
	}

	@Test
	void update_존재하지않는_Tool은_AI_TOOL_NOT_FOUND() {
		given(aiToolRepository.findById(99L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> adminAiToolService.update(
			1L, 99L,
			new AiToolUpdateRequest(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)
		)).isInstanceOf(CustomException.class);
	}

	@Test
	void update_allowlist에_없는_endpointUrl로_변경시_AI_TOOL_UNSAFE_ENDPOINT_예외() {
		AiTool tool = AiTool.createHttpApiTool(
			"직원정보조회", "설명", "https://hr.example.com", "GET",
			"{\"properties\":{}}", null, SideEffectType.READ_ONLY, AuthType.NONE, null, 5000, 100, 1L
		);
		given(aiToolRepository.findById(1L)).willReturn(Optional.of(tool));
		ssrfSafe = false;

		assertThatThrownBy(() -> adminAiToolService.update(
			1L, 1L,
			new AiToolUpdateRequest(null, null, "https://other.example.com", null, null, null, null, null, null, null, null, null, null, null, null, null)
		)).isInstanceOf(CustomException.class);
	}

	@Test
	void update_HTTP_API_Tool에_datasourceKey_설정시_거부() {
		AiTool tool = AiTool.createHttpApiTool(
			"직원정보조회", "설명", "https://hr.example.com", "GET",
			"{\"properties\":{}}", null, SideEffectType.READ_ONLY, AuthType.NONE, null, 5000, 100, 1L
		);
		given(aiToolRepository.findById(1L)).willReturn(Optional.of(tool));

		assertThatThrownBy(() -> adminAiToolService.update(
			1L, 1L,
			new AiToolUpdateRequest(null, null, null, null, "workipediaReadonly", null, null, null, null, null, null, null, null, null, null, null)
		)).isInstanceOf(CustomException.class);
	}

	@Test
	void update_DB_QUERY_Tool에_endpointUrl_설정시_거부() {
		AiTool tool = AiTool.createDbQueryTool(
			"휴가잔여일조회", "설명", "workipediaReadonly",
			"SELECT name FROM employee_vacations LIMIT 1",
			"{\"properties\":{}}", null, 3000, 10, 1L
		);
		given(aiToolRepository.findById(2L)).willReturn(Optional.of(tool));

		assertThatThrownBy(() -> adminAiToolService.update(
			1L, 2L,
			new AiToolUpdateRequest(null, null, "https://hr.example.com", null, null, null, null, null, null, null, null, null, null, null, null, null)
		)).isInstanceOf(CustomException.class);
	}

	@Test
	void healthCheck_HTTP_API_Tool은_HttpApiHealthChecker_호출() {
		AiTool tool = AiTool.createHttpApiTool(
			"직원정보조회", "설명", "https://hr.example.com", "GET",
			"{\"properties\":{}}", null, SideEffectType.READ_ONLY, AuthType.NONE, null, 5000, 100, 1L
		);
		given(aiToolRepository.findById(1L)).willReturn(Optional.of(tool));
		given(httpApiHealthChecker.check(tool)).willReturn(HealthCheckResult.success(120));

		HealthCheckResponse response = adminAiToolService.healthCheck(1L);

		assertThat(response.success()).isTrue();
		assertThat(response.toolType()).isEqualTo("HTTP_API");
	}

	@Test
	void healthCheck_MUTATING_HTTP_API_Tool은_실제_호출하지_않는다() {
		AiTool tool = AiTool.createHttpApiTool(
			"휴가신청", "설명", "https://hr.example.com/api/vacations", "POST",
			"{\"properties\":{}}", null, SideEffectType.MUTATING,
			AuthType.NONE, null, 5000, 100, 1L
		);
		given(aiToolRepository.findById(1L)).willReturn(Optional.of(tool));

		HealthCheckResponse response = adminAiToolService.healthCheck(1L);

		assertThat(response.success()).isFalse();
		assertThat(response.errorMessage()).contains("MUTATING");
		verifyNoInteractions(httpApiHealthChecker);
	}

	@Test
	void healthCheck_DB_QUERY_Tool은_DbQueryHealthChecker_호출() {
		AiTool tool = AiTool.createDbQueryTool(
			"휴가잔여일조회", "설명", "workipediaReadonly",
			"SELECT name FROM employee_vacations LIMIT 1",
			"{\"properties\":{}}", null, 3000, 10, 1L
		);
		given(aiToolRepository.findById(2L)).willReturn(Optional.of(tool));
		given(dbQueryHealthChecker.check(tool)).willReturn(HealthCheckResult.failure(50, "DB 연결에 실패했습니다."));

		HealthCheckResponse response = adminAiToolService.healthCheck(2L);

		assertThat(response.success()).isFalse();
		assertThat(response.toolType()).isEqualTo("DB_QUERY");
	}

	@Test
	void healthCheck_존재하지않는_Tool은_AI_TOOL_NOT_FOUND() {
		given(aiToolRepository.findById(99L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> adminAiToolService.healthCheck(99L))
			.isInstanceOf(CustomException.class);
	}
}
