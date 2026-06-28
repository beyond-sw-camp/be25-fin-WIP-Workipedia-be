package com.wip.workipedia.tool.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.tool.domain.AccessScope;
import com.wip.workipedia.tool.domain.AiTool;
import com.wip.workipedia.tool.domain.ApprovalStatus;
import com.wip.workipedia.tool.domain.ToolExecutionLog;
import com.wip.workipedia.tool.domain.ToolType;
import com.wip.workipedia.tool.dto.ActiveAiToolResponse;
import com.wip.workipedia.tool.dto.ToolExecuteResponse;
import com.wip.workipedia.tool.exception.ToolExecutionException;
import com.wip.workipedia.tool.executor.DbQueryToolExecutor;
import com.wip.workipedia.tool.executor.HttpApiToolExecutor;
import com.wip.workipedia.tool.executor.ToolExecutionResult;
import com.wip.workipedia.tool.repository.AiToolRepository;
import com.wip.workipedia.tool.repository.ToolExecutionLogRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ToolExecutionService {

	private final AiToolRepository aiToolRepository;
	private final ToolExecutionLogRepository toolExecutionLogRepository;
	private final HttpApiToolExecutor httpApiToolExecutor;
	private final DbQueryToolExecutor dbQueryToolExecutor;
	private final ParameterSchemaValidator parameterSchemaValidator;
	private final ObjectMapper objectMapper;

	// AI가 고를 수 있는 후보 Tool 목록. 활성+승인+미삭제 셋 다 만족해야 노출된다(AiTool.isExecutable()과 동일 조건).
	@Transactional(readOnly = true)
	public List<ActiveAiToolResponse> findActiveTools() {
		return aiToolRepository.findByIsActiveAndApprovalStatusAndIsDeleted("Y", ApprovalStatus.APPROVED, "N")
			.stream()
			.map(ActiveAiToolResponse::from)
			.toList();
	}

	// Tool 실행 entry point. 실행 가능 여부 → 파라미터 스키마 검증 → 실제 실행 순으로 체크하고, 성공/실패 모두 audit 로그(ToolExecutionLog)를 남긴다.
	// ToolExecutionException은 여기서 끝까지 잡아서 HTTP 200 + success:false로 변환한다 — AI 입장에서 "Tool 실행 실패"는
	// API 에러가 아니라 흔히 있는 결과라서, GlobalExceptionHandler까지 올려보내지 않는다.
	@Transactional
	public ToolExecuteResponse execute(String caller, Long aiToolId, Map<String, Object> parameters, String callerEmployeeId) {
		AiTool tool = aiToolRepository.findByAiToolIdAndIsDeleted(aiToolId, "N")
			.orElseThrow(() -> new CustomException(ErrorType.AI_TOOL_NOT_FOUND));

		long startedAt = System.currentTimeMillis();
		Map<String, Object> effectiveParameters = applyAccessPolicy(tool, parameters, callerEmployeeId);
		String maskedParametersJson = maskParametersAsJson(effectiveParameters);

		if (!tool.isExecutable()) {
			recordLog(tool, caller, maskedParametersJson, null, elapsed(startedAt), false, "AI_TOOL_NOT_EXECUTABLE");
			throw new CustomException(ErrorType.AI_TOOL_NOT_EXECUTABLE);
		}

		ParameterSchemaValidator.ValidationResult validation =
			parameterSchemaValidator.validate(tool.getParametersSchema(), effectiveParameters);
		if (!validation.valid()) {
			recordLog(tool, caller, maskedParametersJson, null, elapsed(startedAt), false, "AI_TOOL_PARAMETER_MISMATCH");
			throw new CustomException(ErrorType.AI_TOOL_PARAMETER_MISMATCH, validation.message());
		}

		try {
			ToolExecutionResult result = executeByType(tool, effectiveParameters);
			recordLog(tool, caller, maskedParametersJson, result.resultCount(), elapsed(startedAt), true, null);
			return ToolExecuteResponse.success(result.data());
		} catch (ToolExecutionException e) {
			recordLog(tool, caller, maskedParametersJson, null, elapsed(startedAt), false, e.getErrorCode());
			return ToolExecuteResponse.failure(e.getErrorCode(), e.getMessage());
		}
	}

	private Map<String, Object> applyAccessPolicy(AiTool tool, Map<String, Object> parameters, String callerEmployeeId) {
		Map<String, Object> effective = new LinkedHashMap<>(parameters != null ? parameters : Map.of());
		if (tool.getAccessScope() != AccessScope.SELF_ONLY) {
			return effective;
		}
		if (callerEmployeeId == null || callerEmployeeId.isBlank()) {
			throw new CustomException(ErrorType.AI_TOOL_PARAMETER_MISMATCH, "호출자 본인 Tool은 callerEmployeeId가 필요합니다.");
		}
		String selfIdentityParam = tool.getSelfIdentityParam();
		if (selfIdentityParam == null || selfIdentityParam.isBlank()) {
			throw new CustomException(ErrorType.AI_TOOL_PARAMETER_MISMATCH, "호출자 본인 Tool의 selfIdentityParam 설정이 없습니다.");
		}
		effective.put(selfIdentityParam, callerEmployeeId.trim());
		return effective;
	}

	// toolType별로 맞는 executor(HttpApiToolExecutor/DbQueryToolExecutor)에 실행을 위임한다.
	private ToolExecutionResult executeByType(AiTool tool, Map<String, Object> parameters) {
		if (tool.getToolType() == ToolType.HTTP_API) {
			return httpApiToolExecutor.execute(tool, parameters);
		}
		if (tool.getToolType() == ToolType.DB_QUERY) {
			return dbQueryToolExecutor.execute(tool, parameters);
		}
		throw new CustomException(ErrorType.AI_TOOL_INVALID_TYPE);
	}

	private void recordLog(
		AiTool tool, String caller, String maskedParametersJson,
		Integer resultCount, long durationMs, boolean success, String errorCode
	) {
		toolExecutionLogRepository.save(ToolExecutionLog.of(
			tool.getAiToolId(), caller, maskedParametersJson, resultCount, durationMs, success, errorCode
		));
	}

	private long elapsed(long startedAt) {
		return System.currentTimeMillis() - startedAt;
	}

	// 감사 로그에는 파라미터 값이 아니라 키만 남긴다 — 사번 등 입력값 원문이 로그에 쌓이지 않도록.
	private String maskParametersAsJson(Map<String, Object> parameters) {
		if (parameters == null || parameters.isEmpty()) {
			return "{}";
		}
		Map<String, Object> masked = new LinkedHashMap<>();
		parameters.keySet().forEach(key -> masked.put(key, "***"));
		try {
			return objectMapper.writeValueAsString(masked);
		} catch (Exception e) {
			return "{}";
		}
	}
}
