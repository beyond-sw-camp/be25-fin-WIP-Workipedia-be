package com.wip.workipedia.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
	@NotBlank(message = "부서명을 입력해주세요.")
	@Size(max = 100, message = "부서명은 100자 이하여야 합니다.")
	String departmentName
) {
}
