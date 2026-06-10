package com.wip.workipedia.auth.dto;

// 토큰 재발급 성공 시 컨트롤러가 프론트에 반환하기 위한 외부용 DTO입니다.
// 새 Refresh Token은 Body에 포함하지 않고 Set-Cookie 헤더로 전달합니다.
public record TokenRefreshResponse(
	String accessToken
) {
}
