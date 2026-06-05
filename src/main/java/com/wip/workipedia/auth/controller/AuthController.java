package com.wip.workipedia.auth.controller;

import com.wip.workipedia.auth.dto.EmailCodeSendRequest;
import com.wip.workipedia.auth.dto.EmailCodeVerifyRequest;
import com.wip.workipedia.auth.dto.LoginRequest;
import com.wip.workipedia.auth.dto.LoginResponse;
import com.wip.workipedia.auth.dto.LoginResult;
import com.wip.workipedia.auth.dto.SignupRequest;
import com.wip.workipedia.auth.dto.SignupResponse;
import com.wip.workipedia.auth.dto.TokenRefreshResponse;
import com.wip.workipedia.auth.dto.TokenRefreshResult;
import com.wip.workipedia.auth.service.AuthService;
import com.wip.workipedia.auth.service.SignupEmailCodeService;
import com.wip.workipedia.common.security.JwtProperties;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
	private static final String REFRESH_TOKEN_COOKIE_PATH = "/api/v1/auth";

	private final AuthService authService;
	private final SignupEmailCodeService signupEmailCodeService;
	private final JwtProperties jwtProperties;

	// 회원가입 인증코드 발송
	@PostMapping("/signup/code")
	public ResponseEntity<Void> sendSignupCode(
		@Valid @RequestBody EmailCodeSendRequest emailCodeSendRequest
	) {
		signupEmailCodeService.sendSignupCode(emailCodeSendRequest);

		return ResponseEntity.ok().build();
	}

	// 회원가입 인증코드 확인
	@PostMapping("/signup/code/verify")
	public ResponseEntity<Void> verifySignupCode(
		@Valid @RequestBody EmailCodeVerifyRequest emailCodeVerifyRequest
	) {
		signupEmailCodeService.verifySignupCode(emailCodeVerifyRequest);

		return ResponseEntity.ok().build();
	}

	// 회원가입
	@PostMapping("/signup")
	public ResponseEntity<SignupResponse> signup(
		@Valid @RequestBody SignupRequest signupRequest
	) {
		SignupResponse signupResponse = authService.signup(signupRequest);

		return ResponseEntity.status(HttpStatus.CREATED).body(signupResponse);
	}

	// 로그인
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(
		@Valid @RequestBody LoginRequest loginRequest
	) {
		LoginResult loginResult = authService.login(loginRequest);

		return ResponseEntity.ok()
			.header("Set-Cookie", createRefreshTokenCookie(loginResult.refreshToken()).toString())
			.body(loginResult.loginResponse());
	}

	// 토큰 재발급
	@PostMapping("/token/refresh")
	public ResponseEntity<TokenRefreshResponse> refreshToken(
		@CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken
	) {
		TokenRefreshResult tokenRefreshResult = authService.refreshToken(refreshToken);
		TokenRefreshResponse tokenRefreshResponse = new TokenRefreshResponse(tokenRefreshResult.accessToken());

		return ResponseEntity.ok()
			.header("Set-Cookie", createRefreshTokenCookie(tokenRefreshResult.refreshToken()).toString())
			.body(tokenRefreshResponse);
	}

	private ResponseCookie createRefreshTokenCookie(String refreshToken) {
		return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
			.httpOnly(true)
			.secure(true)
			.sameSite("Lax")
			.path(REFRESH_TOKEN_COOKIE_PATH)
			.maxAge(jwtProperties.refreshTokenExpiration())
			.build();
	}
}
