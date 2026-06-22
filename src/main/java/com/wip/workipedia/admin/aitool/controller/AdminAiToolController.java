package com.wip.workipedia.admin.aitool.controller;

import com.wip.workipedia.admin.aitool.dto.AiToolCreateRequest;
import com.wip.workipedia.admin.aitool.dto.AiToolResponse;
import com.wip.workipedia.admin.aitool.dto.AiToolUpdateRequest;
import com.wip.workipedia.admin.aitool.dto.HealthCheckRequest;
import com.wip.workipedia.admin.aitool.dto.HealthCheckResponse;
import com.wip.workipedia.admin.aitool.service.AdminAiToolService;
import com.wip.workipedia.common.request.BasePageRequest;
import com.wip.workipedia.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/admin/ai-tools")
@RequiredArgsConstructor
public class AdminAiToolController {

	private final AdminAiToolService adminAiToolService;

	// 관리자 화면 Tool 목록, 최신 등록 순으로 페이지네이션.
	@GetMapping
	public ResponseEntity<PageResponse<AiToolResponse>> findAll(@Valid BasePageRequest pageRequest) {
		Sort sort = Sort.by(Sort.Direction.DESC, "aiToolId");
		return ResponseEntity.ok(adminAiToolService.findAll(pageRequest.toPageable(sort)));
	}

	// HTTP_API/DB_QUERY Tool 등록. 둘 중 어느 타입인지는 request.toolType()으로 받아 서비스 내부에서 분기한다.
	@PostMapping
	public ResponseEntity<AiToolResponse> create(
		@AuthenticationPrincipal Long adminUserId,
		@Valid @RequestBody AiToolCreateRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(adminAiToolService.create(adminUserId, request));
	}

	// 설정 변경(엔드포인트, 인증 등) + 승인 상태/활성 여부 변경을 같은 요청으로 처리한다.
	@PatchMapping("/{aiToolId}")
	public ResponseEntity<AiToolResponse> update(
		@AuthenticationPrincipal Long adminUserId,
		@PathVariable Long aiToolId,
		@RequestBody AiToolUpdateRequest request
	) {
		return ResponseEntity.ok(adminAiToolService.update(adminUserId, aiToolId, request));
	}

	// 이미 등록된 Tool을 대상으로 재검증한다 (목록/상세 화면의 "연결 체크").
	@PostMapping("/{aiToolId}/health-check")
	public ResponseEntity<HealthCheckResponse> healthCheck(@PathVariable Long aiToolId) {
		return ResponseEntity.ok(adminAiToolService.healthCheck(aiToolId));
	}

	// 등록 화면에서 아직 저장하지 않은 입력값을 미리 체크한다. aiToolId가 없는 시점이라 별도 경로로 둔다.
	@PostMapping("/health-check")
	public ResponseEntity<HealthCheckResponse> healthCheckDraft(@Valid @RequestBody HealthCheckRequest request) {
		return ResponseEntity.ok(adminAiToolService.healthCheckDraft(request));
	}
}
