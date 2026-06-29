package com.wip.workipedia.departmentsync.controller;

import com.wip.workipedia.common.response.ApiResponse;
import com.wip.workipedia.departmentsync.dto.ErpFetchResponse;
import com.wip.workipedia.departmentsync.dto.SyncApplyRequest;
import com.wip.workipedia.departmentsync.dto.SyncApplyResponse;
import com.wip.workipedia.departmentsync.dto.SyncPreviewRequest;
import com.wip.workipedia.departmentsync.dto.SyncPreviewResponse;
import com.wip.workipedia.departmentsync.service.DepartmentSyncService;
import com.wip.workipedia.departmentsync.service.ErpFetchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/departments/sync")
@RequiredArgsConstructor
public class DepartmentSyncController {

	private final DepartmentSyncService syncService;
	private final ErpFetchService erpFetchService;

	public record FetchRequest(String url) {}

	// ERP 부서 API URL을 받아 BE가 직접 조회(CORS 회피)한다.
	@PostMapping("/fetch")
	public ResponseEntity<ApiResponse<ErpFetchResponse>> fetch(@RequestBody FetchRequest request) {
		return ApiResponse.success(HttpStatus.OK, "ERP 조회 성공", erpFetchService.fetch(request.url()));
	}

	// 들어온 부서 목록과 현재 운영 부서를 비교해 diff를 미리보기한다.
	@PostMapping("/preview")
	public ResponseEntity<ApiResponse<SyncPreviewResponse>> preview(
		@Valid @RequestBody SyncPreviewRequest request
	) {
		return ApiResponse.success(HttpStatus.OK, "동기화 미리보기", syncService.preview(request));
	}

	// 검토 완료된 변경을 운영에 반영한다.
	@PostMapping("/apply")
	public ResponseEntity<ApiResponse<SyncApplyResponse>> apply(
		@Valid @RequestBody SyncApplyRequest request
	) {
		// TODO: 인증 도입 후 SecurityUtil로 actorUserId 대체. 현재 skeleton: 1L
		Long actorUserId = 1L;
		return ApiResponse.success(HttpStatus.OK, "동기화 반영 완료", syncService.apply(request, actorUserId));
	}
}
