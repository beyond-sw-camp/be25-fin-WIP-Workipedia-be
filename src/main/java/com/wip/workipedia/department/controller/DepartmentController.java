package com.wip.workipedia.department.controller;

import com.wip.workipedia.common.response.ApiResponse;
import com.wip.workipedia.department.dto.DepartmentResponse;
import com.wip.workipedia.department.service.DepartmentService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

	private final DepartmentService departmentService;

	public DepartmentController(DepartmentService departmentService) {
		this.departmentService = departmentService;
	}

	@GetMapping
	// 회원가입 화면의 부서 선택창에 표시할 부서 목록을 조회합니다.
	public ResponseEntity<ApiResponse<List<DepartmentResponse>>> findAll() {
		List<DepartmentResponse> departmentResponses = departmentService.findAll();

		return ApiResponse.success(HttpStatus.OK, "부서 목록 조회 성공", departmentResponses);
	}
}
