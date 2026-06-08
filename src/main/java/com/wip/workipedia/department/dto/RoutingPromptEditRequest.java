package com.wip.workipedia.department.dto;

import jakarta.validation.constraints.NotBlank;

public record RoutingPromptEditRequest(
	@NotBlank(message = "부서 역할 설명 수정 명령을 입력해주세요.")
	String instruction
) {
}
