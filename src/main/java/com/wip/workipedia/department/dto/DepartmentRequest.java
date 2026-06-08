package com.wip.workipedia.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
	@NotBlank(message = "부서명을 입력해주세요.")
	@Size(max = 50, message = "부서명은 50자 이하여야 합니다.")
	String departmentName
) {
}
