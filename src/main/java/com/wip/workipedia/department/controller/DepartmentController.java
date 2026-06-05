package com.wip.workipedia.department.controller;

import com.wip.workipedia.department.dto.DepartmentResponse;
import com.wip.workipedia.department.service.DepartmentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

	private final DepartmentService departmentService;

	// 회원가입 부서 목록 조회
	@GetMapping
	public ResponseEntity<List<DepartmentResponse>> findAll() {
		List<DepartmentResponse> departmentResponses = departmentService.findAll();

		return ResponseEntity.ok(departmentResponses);
	}
}
