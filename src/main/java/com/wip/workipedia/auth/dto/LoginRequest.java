package com.wip.workipedia.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
	@NotBlank(message = "사번을 입력해주세요.")
	String employeeId,

	@NotBlank(message = "비밀번호를 입력해주세요.")
	String password
) {
}
