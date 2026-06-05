package com.wip.workipedia.department.controller;

import com.wip.workipedia.department.dto.DepartmentRequest;
import com.wip.workipedia.department.dto.DepartmentResponse;
import com.wip.workipedia.department.service.DepartmentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/departments")
@RequiredArgsConstructor
public class AdminDepartmentController {
	private final DepartmentService departmentService;

	// 부서 목록 조회
	@GetMapping
	public ResponseEntity<List<DepartmentResponse>> findAll() {
		return ResponseEntity.ok(departmentService.findAll());
	}

	// 부서 등록
	@PostMapping
	public ResponseEntity<DepartmentResponse> create(@Valid @RequestBody DepartmentRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.create(request));
	}

	// 부서 정보 수정
	@PatchMapping("/{departmentId}")
	public ResponseEntity<DepartmentResponse> update(
			@PathVariable Long departmentId,
			@Valid @RequestBody DepartmentRequest request
	) {
		return ResponseEntity.ok(departmentService.update(departmentId, request));
	}

	// 부서 삭제
	@DeleteMapping("/{departmentId}")
	public ResponseEntity<Void> delete(@PathVariable Long departmentId) {
		departmentService.delete(departmentId);
		return ResponseEntity.noContent().build();
	}
}
