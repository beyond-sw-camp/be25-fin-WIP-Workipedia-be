package com.wip.workipedia.department.dto;

import jakarta.validation.constraints.NotBlank;

public record RoutingPromptEditRequest(
	@NotBlank(message = "부서 역할 설명 입력 내용을 작성해주세요.")
	String instruction
) {
}
