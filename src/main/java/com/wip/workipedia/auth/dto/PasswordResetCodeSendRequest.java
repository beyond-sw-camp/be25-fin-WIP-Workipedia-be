package com.wip.workipedia.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetCodeSendRequest(
	@NotBlank(message = "사번을 입력해주세요.")
	String employeeId,

	@NotBlank(message = "이메일을 입력해주세요.")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	String email
) {
}
