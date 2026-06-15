package com.wip.workipedia.admin.user.controller;

import com.wip.workipedia.admin.user.dto.AdminUserResponse;
import com.wip.workipedia.admin.user.dto.AdminUserStatusRequest;
import com.wip.workipedia.admin.user.service.AdminUserService;
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
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

	private final AdminUserService adminUserService;

	@GetMapping
	public ResponseEntity<PageResponse<AdminUserResponse>> findAll(@Valid BasePageRequest pageRequest) {
		Sort sort = Sort.by(Sort.Direction.ASC, "userId");
		return ResponseEntity.ok(adminUserService.findAll(pageRequest.toPageable(sort)));
	}

	@GetMapping("/search")
	public ResponseEntity<AdminUserResponse> search(
			@NotBlank(message = "사번을 입력해주세요.")
			@RequestParam String employeeId
	) {
		return ResponseEntity.ok(adminUserService.search(employeeId));
	}

	@PatchMapping("/{userId}/status")
	public ResponseEntity<AdminUserResponse> changeStatus(
			@PathVariable Long userId,
			@Valid @RequestBody AdminUserStatusRequest request
	) {
		return ResponseEntity.ok(adminUserService.changeStatus(userId, request.status()));
	}
}
