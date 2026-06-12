package com.wip.workipedia.admin.point.controller;

import com.wip.workipedia.admin.point.dto.AdminPointDeductRequest;
import com.wip.workipedia.admin.point.dto.AdminPointResponse;
import com.wip.workipedia.admin.point.service.AdminPointService;
import com.wip.workipedia.common.request.BasePageRequest;
import com.wip.workipedia.common.response.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/admin/points")
@RequiredArgsConstructor
public class AdminPointController {

	private final AdminPointService adminPointService;

	// 전체 사용자 포인트 목록을 페이징으로 조회한다.
	@GetMapping
	public ResponseEntity<PageResponse<AdminPointResponse>> findAll(@Valid BasePageRequest pageRequest) {
		Sort sort = Sort.by(Sort.Direction.ASC, "userId");
		return ResponseEntity.ok(adminPointService.findAll(pageRequest.toPageable(sort)));
	}

	// 사번으로 사용자를 찾고, 해당 사용자의 현재 보유 포인트를 조회한다.
	@GetMapping("/search")
	public ResponseEntity<AdminPointResponse> search(
			@NotBlank(message = "사번을 입력해주세요.")
			@RequestParam String employeeId
	) {
		return ResponseEntity.ok(adminPointService.search(employeeId));
	}

	// 사번 기준으로 사용자를 찾은 뒤, 요청한 amount만큼 포인트를 차감한다.
	@PatchMapping("/{employeeId}/deduct")
	public ResponseEntity<AdminPointResponse> deduct(
			@PathVariable String employeeId,
			@Valid @RequestBody AdminPointDeductRequest request
	) {
		return ResponseEntity.ok(adminPointService.deduct(employeeId, request.amount(), request.reason()));
	}
}
