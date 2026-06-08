package com.wip.workipedia.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordResetRequest(
	@NotBlank(message = "사번을 입력해주세요.")
	String employeeId,

	@NotBlank(message = "이메일을 입력해주세요.")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	String email,

	@NotBlank(message = "새 비밀번호를 입력해주세요.")
	@Size(min = 8, max = 16, message = "비밀번호는 8~16자여야 합니다.")
	@Pattern(
		regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,16}$",
		message = "비밀번호는 영문자, 숫자 조합의 8~16자리를 사용해야 합니다."
	)
	String newPassword
) {
}
