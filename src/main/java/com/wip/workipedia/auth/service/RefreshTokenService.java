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

	public void save(
		Long userId,
		String refreshToken
	) {
		stringRedisTemplate.opsForValue()
			.set(createRefreshTokenKey(userId), refreshToken, jwtProperties.refreshTokenExpiration());
	}

	public boolean matches(
		Long userId,
		String refreshToken
	) {
		String savedRefreshToken = stringRedisTemplate.opsForValue()
			.get(createRefreshTokenKey(userId));

		return refreshToken.equals(savedRefreshToken);
	}

	public void delete(Long userId) {
		stringRedisTemplate.delete(createRefreshTokenKey(userId));
	}

	private String createRefreshTokenKey(Long userId) {
		return REFRESH_TOKEN_KEY_PREFIX + userId;
	}
}
