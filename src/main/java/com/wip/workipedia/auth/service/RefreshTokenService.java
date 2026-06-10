package com.wip.workipedia.auth.service;

import com.wip.workipedia.common.security.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

	private static final String REFRESH_TOKEN_KEY_PREFIX = "auth:refresh-token:";

	private final StringRedisTemplate stringRedisTemplate;
	private final JwtProperties jwtProperties;

	// userId 기준 Redis key에 Refresh Token을 저장하거나 기존 값을 새 토큰으로 덮어씁니다.
	public void save(
		Long userId,
		String refreshToken
	) {
		stringRedisTemplate.opsForValue()
			.set(createRefreshTokenKey(userId), refreshToken, jwtProperties.refreshTokenExpiration());
	}

	// 요청된 Refresh Token이 Redis에 저장된 현재 토큰과 일치하는지 확인합니다.
	public boolean matches(
		Long userId,
		String refreshToken
	) {
		String savedRefreshToken = stringRedisTemplate.opsForValue()
			.get(createRefreshTokenKey(userId));

		return refreshToken.equals(savedRefreshToken);
	}

	// userId 기준으로 Redis에 저장된 Refresh Token을 삭제합니다.
	public void delete(Long userId) {
		stringRedisTemplate.delete(createRefreshTokenKey(userId));
	}

	// Refresh Token 저장에 사용할 Redis key를 생성합니다.
	private String createRefreshTokenKey(Long userId) {
		return REFRESH_TOKEN_KEY_PREFIX + userId;
	}
}
