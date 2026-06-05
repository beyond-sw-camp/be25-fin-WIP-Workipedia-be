package com.wip.workipedia.auth.dto;

// 서비스에서 컨트롤러로 새로 발급한 Access Token과 Refresh Token을 함께 전달하기 위한 내부용 DTO입니다.
// 프론트에 직접 반환하지 않고, 컨트롤러에서 응답 Body와 Set-Cookie 헤더로 나누어 사용합니다.
public record TokenRefreshResult(
	String accessToken,

	String refreshToken
) {
}
