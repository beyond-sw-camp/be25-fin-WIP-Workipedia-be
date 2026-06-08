package com.wip.workipedia.department.dto;

import jakarta.validation.constraints.NotBlank;

public record DepartmentRoutingPromptRequest(
	@NotBlank(message = "부서 역할 설명을 입력해주세요.")
	String routingPrompt
) {
}
