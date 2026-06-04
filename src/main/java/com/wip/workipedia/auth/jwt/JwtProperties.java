package com.wip.workipedia.auth.jwt;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record JwtProperties(
	String secret,

	Duration accessTokenExpiration,

	Duration refreshTokenExpiration
) {
	public JwtProperties(
		@Value("${app.jwt.secret:local-development-jwt-secret-key-change-before-release}")
		String secret,

		@Value("${app.jwt.access-token-expiration:15m}")
		Duration accessTokenExpiration,

		@Value("${app.jwt.refresh-token-expiration:14d}")
		Duration refreshTokenExpiration
	) {
		this.secret = secret;
		this.accessTokenExpiration = accessTokenExpiration;
		this.refreshTokenExpiration = refreshTokenExpiration;
	}
}
