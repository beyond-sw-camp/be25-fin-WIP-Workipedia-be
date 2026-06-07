package com.wip.workipedia.common.security;

// 로그인 및 JWT 인증 구현이 완료되기 전까지 로컬 개발과 API 테스트에서 사용할 임시 보안 유틸입니다.
// 예를 들어 워키 작성, 티켓 생성, 알림 조회처럼 현재 사용자 ID가 필요한 기능에서 아래 코드를 호출하여 임시 ID를 사용할 수 있습니다.
// Long userId = SecurityUtil.getCurrentUserId();

// 로컬 DB에 user_id가 1인 사용자가 있어야 정상 테스트할 수 있습니다.
public final class SecurityUtil {
	private SecurityUtil() {
	}

	public static Long getCurrentUserId() {
			// TODO: JWT 인증 구현 완료 후 SecurityContext 기반 사용자 ID 조회 방식으로 교체
		return 1L;
	}
}
