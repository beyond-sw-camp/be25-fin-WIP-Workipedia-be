package com.wip.workipedia.auth.dto;

public record LoginResponse(
	String accessToken,

	Long userId,

	Long departmentId,

	String role,

	String nickname,

	String status
) {
}
