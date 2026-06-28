package com.wip.workipedia.tool.controller;

import com.wip.workipedia.tool.dto.ActiveAiToolResponse;
import com.wip.workipedia.tool.dto.ToolExecuteRequest;
import com.wip.workipedia.tool.dto.ToolExecuteResponse;
import com.wip.workipedia.tool.service.ToolExecutionService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/ai-tools")
@RequiredArgsConstructor
public class InternalAiToolController {

	// 호출자 식별용 상수. AI 서버 하나만 이 경로를 호출하므로(X-Internal-Api-Key로 인증) 별도 신원 구분 없이 고정값을 감사 로그에 남긴다.
	private static final String CALLER = "ai-server";

	private final ToolExecutionService toolExecutionService;

	// AI가 사용할 수 있는 Tool 목록(활성+승인된 것만). AI 오케스트레이터의 Tool 선택 단계에서 호출한다.
	@GetMapping("/active")
	public ResponseEntity<List<ActiveAiToolResponse>> getActiveTools() {
		return ResponseEntity.ok(toolExecutionService.findActiveTools());
	}

	// AI가 고른 Tool을 실제로 실행한다. parameters는 AI가 채운 값이며, 검증·실행·감사 로그는 서비스에서 처리한다.
	@PostMapping("/{aiToolId}/execute")
	public ResponseEntity<ToolExecuteResponse> execute(
		@PathVariable Long aiToolId,
		@RequestBody ToolExecuteRequest request
	) {
		Map<String, Object> parameters = request.parameters() != null ? request.parameters() : Map.of();
		return ResponseEntity.ok(toolExecutionService.execute(CALLER, aiToolId, parameters, request.callerEmployeeId()));
	}
}
