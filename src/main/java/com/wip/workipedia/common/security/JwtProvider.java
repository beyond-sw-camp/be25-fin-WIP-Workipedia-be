package com.wip.workipedia.common.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.domain.UserRole;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtProvider {

	private static final String HMAC_SHA256 = "HmacSHA256";
	private static final String ACCESS_TOKEN_TYPE = "access";
	private static final String REFRESH_TOKEN_TYPE = "refresh";
	private static final TypeReference<Map<String, Object>> CLAIMS_TYPE = new TypeReference<>() {
	};

	private final JwtProperties jwtProperties;
	private final ObjectMapper objectMapper;

	public String createAccessToken(User user) {
		return createToken(user, ACCESS_TOKEN_TYPE, jwtProperties.accessTokenExpiration());
	}

	public String createRefreshToken(User user) {
		return createToken(user, REFRESH_TOKEN_TYPE, jwtProperties.refreshTokenExpiration());
	}

	public boolean isValidAccessToken(String token) {
		return isValidToken(token, ACCESS_TOKEN_TYPE);
	}

	public boolean isValidRefreshToken(String token) {
		return isValidToken(token, REFRESH_TOKEN_TYPE);
	}

	public Long getUserId(String token) {
		Map<String, Object> claims = parseClaims(token);

		return Long.valueOf(claims.get("sub").toString());
	}

	public UserRole getRole(String token) {
		Map<String, Object> claims = parseClaims(token);

		return UserRole.valueOf(claims.get("role").toString());
	}

	private String createToken(
		User user,
		String tokenType,
		java.time.Duration expiration
	) {
		Instant now = Instant.now();
		Map<String, Object> header = new LinkedHashMap<>();
		header.put("alg", "HS256");
		header.put("typ", "JWT");

		Map<String, Object> claims = new LinkedHashMap<>();
		claims.put("sub", user.getUserId());
		claims.put("employeeId", user.getEmployeeId());
		claims.put("role", user.getRole().name());
		claims.put("type", tokenType);
		claims.put("iat", now.getEpochSecond());
		claims.put("exp", now.plus(expiration).getEpochSecond());

		String unsignedToken = encodeJson(header) + "." + encodeJson(claims);

		return unsignedToken + "." + sign(unsignedToken);
	}

	private boolean isValidToken(
		String token,
		String expectedTokenType
	) {
		try {
			if (!isSignatureValid(token)) {
				return false;
			}

			Map<String, Object> claims = parseClaims(token);
			long expiration = Long.parseLong(claims.get("exp").toString());
			String tokenType = claims.get("type").toString();

			return expectedTokenType.equals(tokenType) && Instant.now().getEpochSecond() < expiration;
		} catch (RuntimeException exception) {
			return false;
		}
	}

	private boolean isSignatureValid(String token) {
		String[] parts = token.split("\\.");
		if (parts.length != 3) {
			return false;
		}

		String unsignedToken = parts[0] + "." + parts[1];
		String expectedSignature = sign(unsignedToken);

		return expectedSignature.equals(parts[2]);
	}

	private Map<String, Object> parseClaims(String token) {
		String[] parts = token.split("\\.");
		if (parts.length != 3) {
			throw new IllegalArgumentException("Invalid JWT token");
		}

		try {
			byte[] decodedPayload = Base64.getUrlDecoder().decode(parts[1]);

			return objectMapper.readValue(decodedPayload, CLAIMS_TYPE);
		} catch (IOException exception) {
			throw new IllegalArgumentException("Invalid JWT payload", exception);
		}
	}

	private String encodeJson(Map<String, Object> value) {
		try {
			byte[] json = objectMapper.writeValueAsBytes(value);

			return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Failed to create JWT payload", exception);
		}
	}

	private String sign(String value) {
		try {
			Mac mac = Mac.getInstance(HMAC_SHA256);
			SecretKeySpec keySpec = new SecretKeySpec(
				jwtProperties.secret().getBytes(StandardCharsets.UTF_8),
				HMAC_SHA256
			);
			mac.init(keySpec);

			byte[] signature = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));

			return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
		} catch (Exception exception) {
			throw new IllegalStateException("Failed to sign JWT token", exception);
		}
	}
}
