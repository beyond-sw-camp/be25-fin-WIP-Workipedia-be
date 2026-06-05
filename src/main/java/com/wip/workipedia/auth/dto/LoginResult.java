package com.wip.workipedia.auth.dto;

// 서비스에서 컨트롤러로 로그인 응답 Body와 Refresh Token을 함께 전달하기 위한 내부용 DTO입니다.
// Refresh Token은 프론트에 직접 Body로 반환하지 않고, 컨트롤러에서 Set-Cookie 헤더로 전달합니다.
public record LoginResult(
	LoginResponse loginResponse,

	String refreshToken
) {
}
