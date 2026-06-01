package com.wip.workipedia.auth.dto;

public record SignupResponse(
	Long userId,

	String role,

	String nickname,

	String status
) {
}
