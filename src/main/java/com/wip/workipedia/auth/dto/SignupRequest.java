package com.wip.workipedia.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
	@NotBlank
	String employeeId,

	@NotNull
	Long departmentId,

	@NotBlank
	@Email
	String email,

	@NotBlank
	@Size(min = 8, max = 16)
	@Pattern(
		regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,16}$",
		message = "비밀번호는 영문자, 숫자 조합의 8~16자리를 사용해야 합니다."
	)
	String password
) {
}
