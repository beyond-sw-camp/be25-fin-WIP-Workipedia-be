package com.wip.workipedia.admin.aitool.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.admin.aitool.dto.AiToolCreateRequest;
import com.wip.workipedia.admin.aitool.dto.AiToolResponse;
import com.wip.workipedia.admin.aitool.dto.AiToolUpdateRequest;
import com.wip.workipedia.admin.aitool.dto.HealthCheckRequest;
import com.wip.workipedia.admin.aitool.dto.HealthCheckResponse;
import com.wip.workipedia.admin.domain.AdminLog;
import com.wip.workipedia.admin.repository.AdminLogRepository;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.tool.domain.AiTool;
import com.wip.workipedia.tool.domain.ApprovalStatus;
import com.wip.workipedia.tool.domain.AuthType;
import com.wip.workipedia.tool.domain.ToolType;
import com.wip.workipedia.tool.executor.DbQueryHealthChecker;
import com.wip.workipedia.tool.executor.HealthCheckResult;
import com.wip.workipedia.tool.executor.HttpApiHealthChecker;
import com.wip.workipedia.tool.executor.SsrfGuard;
import com.wip.workipedia.tool.repository.AiToolRepository;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAiToolService {

	private static final Set<String> SUPPORTED_AUTH_TYPES = Set.of("NONE", "API_KEY", "BEARER_TOKEN");
	private static final Set<String> SUPPORTED_HTTP_METHODS = Set.of("GET", "POST", "PUT", "PATCH", "DELETE");
	// credentialRef는 실제 secret 값이 아니라 환경변수 "이름"만 저장해야 한다 — 관례상 대문자/숫자/언더스코어로 구성된 환경변수 이름 형식을 강제해
	// admin이 실수로 raw API key/토큰 값을 직접 붙여넣는 것을 1차로 막는다.
	private static final Pattern CREDENTIAL_REF_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]*$");

	private final AiToolRepository aiToolRepository;
	private final AdminLogRepository adminLogRepository;
	private final ObjectMapper objectMapper;
	private final SsrfGuard ssrfGuard;
	private final HttpApiHealthChecker httpApiHealthChecker;
	private final DbQueryHealthChecker dbQueryHealthChecker;

	// 삭제되지 않은 Tool만 페이지네이션으로 조회한다.
	@Transactional(readOnly = true)
	public PageResponse<AiToolResponse> findAll(Pageable pageable) {
		return PageResponse.from(aiToolRepository.findByIsDeleted("N", pageable).map(AiToolResponse::from));
	}

	// 이미 저장된 Tool을 대상으로 재검증한다 (자격증명 교체, 비활성 Tool 재활성화 전 확인 등).
	@Transactional(readOnly = true)
	public HealthCheckResponse healthCheck(Long aiToolId) {
		AiTool tool = findTool(aiToolId);
		HealthCheckResult result = tool.getToolType() == ToolType.HTTP_API
			? httpApiHealthChecker.check(tool)
			: dbQueryHealthChecker.check(tool);

		return new HealthCheckResponse(result.success(), tool.getToolType().name(), result.latencyMs(), result.errorMessage());
	}

	// 등록 화면에서 아직 저장하지 않은 입력값을 체크한다. DB에 저장하지 않는 임시 AiTool을 메모리에서만 만들어
	// 위 healthCheck()와 동일한 checker에 넘기므로 검증 로직은 공유되고, 결과만 DB에 안 남는다.
	@Transactional(readOnly = true)
	public HealthCheckResponse healthCheckDraft(HealthCheckRequest request) {
		validateToolType(request.toolType());
		int timeoutMs = request.timeoutMs() != null ? request.timeoutMs() : 5000;

		AiTool draft;
		if ("HTTP_API".equals(request.toolType())) {
			// authType 미입력은 "아직 안 골랐다"는 뜻이라 NONE으로 두고 체크 결과(실패)로 사용자에게 알려준다.
			AuthType authType = (request.authType() == null || request.authType().isBlank())
				? AuthType.NONE
				: parseAuthType(request.authType());
			draft = AiTool.createHttpApiTool(
				"draft", "draft", request.endpointUrl(), request.httpMethod(),
				"{}", null, authType, request.credentialRef(), timeoutMs, 1, null
			);
		} else {
			// DbQueryHealthChecker는 queryTemplate을 실행하지 않고 datasourceKey로 "SELECT 1"만 날리므로 더미 값을 채운다.
			draft = AiTool.createDbQueryTool(
				"draft", "draft", request.datasourceKey(), "SELECT 1", "{}", null, timeoutMs, 1, null
			);
		}

		HealthCheckResult result = draft.getToolType() == ToolType.HTTP_API
			? httpApiHealthChecker.check(draft)
			: dbQueryHealthChecker.check(draft);

		return new HealthCheckResponse(result.success(), draft.getToolType().name(), result.latencyMs(), result.errorMessage());
	}

	// Tool 등록. 공통 검증(toolType, authType, JSON Schema) 후 타입별 나머지 검증·생성은 buildTool에 위임하고,
	// 등록 자체는 관리자 행동이라 AdminLog에도 남긴다(Tool 실행 기록인 ToolExecutionLog와는 별개).
	@Transactional
	public AiToolResponse create(Long adminUserId, AiToolCreateRequest request) {
		validateToolType(request.toolType());
		AuthType authType = parseAuthType(request.authType());
		validateJsonSchema(request.parametersSchema());
		AiTool tool = buildTool(adminUserId, request, authType);
		aiToolRepository.save(tool);

		adminLogRepository.save(AdminLog.of(
			adminUserId, "AI_TOOL_CREATE", "AI_TOOL",
			"AI Tool 등록: " + tool.getName(),
			String.format("{\"aiToolId\":%s,\"toolType\":\"%s\"}", tool.getAiToolId(), tool.getToolType())
		));

		return AiToolResponse.from(tool);
	}

	// toolType에 따라 HTTP_API/DB_QUERY 전용 검증을 거친 뒤 해당 타입의 AiTool을 생성한다.
	private AiTool buildTool(Long adminUserId, AiToolCreateRequest request, AuthType authType) {
		if ("HTTP_API".equals(request.toolType())) {
			validateHttpApiConfig(request.endpointUrl(), request.httpMethod());
			validateCredentialRef(authType, request.credentialRef());
			validateEndpointHost(request.endpointUrl());
			return AiTool.createHttpApiTool(
				request.name(), request.description(),
				request.endpointUrl(), request.httpMethod(), request.parametersSchema(), request.responseSchema(),
				authType, request.credentialRef(), request.timeoutMs(), request.maxResultCount(), adminUserId
			);
		}

		validateDbQueryConfig(authType, request.datasourceKey(), request.queryTemplate());
		return AiTool.createDbQueryTool(
			request.name(), request.description(),
			request.datasourceKey(), request.queryTemplate(), request.parametersSchema(), request.responseSchema(),
			request.timeoutMs(), request.maxResultCount(), adminUserId
		);
	}

	// 설정 변경 + 승인 상태/활성 여부 변경을 한 요청으로 처리한다. 필드별로 null이면 "변경 안 함"으로 취급한다.
	@Transactional
	public AiToolResponse update(Long adminUserId, Long aiToolId, AiToolUpdateRequest request) {
		AiTool tool = findTool(aiToolId);
		validateUpdateAgainstToolType(tool.getToolType(), request);

		AuthType authType = request.authType() != null ? parseAuthType(request.authType()) : null;
		if (authType != null) {
			String credentialRef = request.credentialRef() != null ? request.credentialRef() : tool.getCredentialRef();
			validateCredentialRef(authType, credentialRef);
		}
		if (request.parametersSchema() != null) {
			validateJsonSchema(request.parametersSchema());
		}
		if (request.endpointUrl() != null) {
			validateEndpointHost(request.endpointUrl());
		}
		if (request.httpMethod() != null && !SUPPORTED_HTTP_METHODS.contains(request.httpMethod())) {
			throw new CustomException(ErrorType.BAD_REQUEST, "지원하지 않는 httpMethod입니다: " + request.httpMethod());
		}

		tool.updateConfig(
			request.description(), request.endpointUrl(), request.httpMethod(),
			request.datasourceKey(), request.queryTemplate(), request.parametersSchema(), request.responseSchema(),
			authType, request.credentialRef(), request.timeoutMs(), request.maxResultCount(), adminUserId
		);

		if (request.approvalStatus() != null) {
			tool.changeApprovalStatus(parseApprovalStatus(request.approvalStatus()), adminUserId);
		}
		if (request.active() != null) {
			tool.changeActive(request.active(), adminUserId);
		}

		adminLogRepository.save(AdminLog.of(
			adminUserId, "AI_TOOL_UPDATE", "AI_TOOL",
			"AI Tool 설정 변경: " + tool.getName(),
			String.format("{\"aiToolId\":%s}", tool.getAiToolId())
		));

		return AiToolResponse.from(tool);
	}

	// admin 화면(수정/health-check)이 다루는 Tool은 삭제 여부와 무관하게 ID로 바로 찾는다.
	// 목록(findAll)만 isDeleted="N"으로 거르는 이유는 목록에는 삭제된 Tool을 노출하지 않아야 해서다.
	private AiTool findTool(Long aiToolId) {
		return aiToolRepository.findById(aiToolId)
			.orElseThrow(() -> new CustomException(ErrorType.AI_TOOL_NOT_FOUND));
	}

	// M2 범위에서 등록 가능한 toolType을 제한한다.
	private void validateToolType(String toolType) {
		if (!"HTTP_API".equals(toolType) && !"DB_QUERY".equals(toolType)) {
			throw new CustomException(ErrorType.AI_TOOL_INVALID_TYPE, "M2 범위에서는 HTTP_API/DB_QUERY Tool만 등록할 수 있습니다.");
		}
	}

	private void validateHttpApiConfig(String endpointUrl, String httpMethod) {
		if (endpointUrl == null || endpointUrl.isBlank() || httpMethod == null || httpMethod.isBlank()) {
			throw new CustomException(ErrorType.BAD_REQUEST, "HTTP_API Tool은 endpointUrl과 httpMethod가 필요합니다.");
		}
		if (!SUPPORTED_HTTP_METHODS.contains(httpMethod)) {
			throw new CustomException(ErrorType.BAD_REQUEST, "지원하지 않는 httpMethod입니다: " + httpMethod);
		}
	}

	// DB_QUERY Tool은 AI가 SQL을 만들지 못하게 인증을 안 쓰고(authType=NONE), datasource·query 둘 다 필수다.
	private void validateDbQueryConfig(AuthType authType, String datasourceKey, String queryTemplate) {
		if (authType != AuthType.NONE) {
			throw new CustomException(ErrorType.AI_TOOL_INVALID_AUTH_TYPE, "DB_QUERY Tool은 authType=NONE만 허용합니다.");
		}
		if (datasourceKey == null || datasourceKey.isBlank()) {
			throw new CustomException(ErrorType.BAD_REQUEST, "DB_QUERY Tool은 datasourceKey가 필요합니다.");
		}
		if (queryTemplate == null || queryTemplate.isBlank()) {
			throw new CustomException(ErrorType.BAD_REQUEST, "DB_QUERY Tool은 queryTemplate이 필요합니다.");
		}
	}

	// HTTP_API/DB_QUERY 필드가 섞여서 ck_ai_tools_type_configuration DB 제약 위반(처리되지 않은 SQL 예외)으로 이어지지 않게 미리 막는다.
	private void validateUpdateAgainstToolType(ToolType toolType, AiToolUpdateRequest request) {
		if (toolType == ToolType.HTTP_API && (request.datasourceKey() != null || request.queryTemplate() != null)) {
			throw new CustomException(ErrorType.BAD_REQUEST, "HTTP_API Tool은 datasourceKey/queryTemplate을 설정할 수 없습니다.");
		}
		if (toolType == ToolType.DB_QUERY && (request.endpointUrl() != null || request.httpMethod() != null)) {
			throw new CustomException(ErrorType.BAD_REQUEST, "DB_QUERY Tool은 endpointUrl/httpMethod를 설정할 수 없습니다.");
		}
	}

	// 문자열 authType을 enum으로 변환하면서 M2에서 미지원인 OAUTH2를 걸러낸다.
	private AuthType parseAuthType(String authType) {
		AuthType parsed;
		try {
			parsed = AuthType.valueOf(authType);
		} catch (IllegalArgumentException e) {
			throw new CustomException(ErrorType.AI_TOOL_INVALID_AUTH_TYPE);
		}
		if (!SUPPORTED_AUTH_TYPES.contains(parsed.name())) {
			throw new CustomException(ErrorType.AI_TOOL_INVALID_AUTH_TYPE, "M2 범위에서는 OAUTH2 인증을 지원하지 않습니다.");
		}
		return parsed;
	}

	// 인증이 필요한 Tool은 credentialRef(환경변수 이름)가 있어야 하고, 형식도 CREDENTIAL_REF_PATTERN을 따라야 한다.
	private void validateCredentialRef(AuthType authType, String credentialRef) {
		if (authType == AuthType.NONE) {
			return;
		}
		if (credentialRef == null || credentialRef.isBlank()) {
			throw new CustomException(ErrorType.BAD_REQUEST, "인증이 필요한 Tool은 credentialRef를 입력해야 합니다.");
		}
		if (!CREDENTIAL_REF_PATTERN.matcher(credentialRef).matches()) {
			throw new CustomException(
				ErrorType.BAD_REQUEST, "credentialRef는 환경변수 이름 형식(대문자/숫자/언더스코어)이어야 합니다. secret 값을 직접 입력하지 마세요."
			);
		}
	}

	// parametersSchema는 정식 JSON Schema가 아니라 자체 단순 포맷이지만, 그래도 유효한 JSON이어야 한다.
	private void validateJsonSchema(String parametersSchema) {
		try {
			objectMapper.readTree(parametersSchema);
		} catch (Exception e) {
			throw new CustomException(ErrorType.BAD_REQUEST, "parametersSchema가 올바른 JSON 형식이 아닙니다.");
		}
	}

	// endpointUrl이 SsrfGuard allowlist/사설망 차단 조건을 만족하는지 등록·수정 시점에 미리 막는다(실행 시점에도 한 번 더 검사됨).
	private void validateEndpointHost(String endpointUrl) {
		if (!ssrfGuard.isSafe(endpointUrl)) {
			throw new CustomException(ErrorType.AI_TOOL_UNSAFE_ENDPOINT);
		}
	}

	private ApprovalStatus parseApprovalStatus(String approvalStatus) {
		try {
			return ApprovalStatus.valueOf(approvalStatus);
		} catch (IllegalArgumentException e) {
			throw new CustomException(ErrorType.BAD_REQUEST, "올바르지 않은 approvalStatus입니다.");
		}
	}
}
