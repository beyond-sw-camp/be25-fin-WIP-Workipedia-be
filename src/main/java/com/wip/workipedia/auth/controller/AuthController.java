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
	private static final long REFRESH_TOKEN_COOKIE_MAX_AGE_SECONDS = 14 * 24 * 60 * 60;

	private final AuthService authService;
	private final SignupEmailCodeService signupEmailCodeService;

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(
		@Valid @RequestBody LoginRequest loginRequest
	) {
		LoginResult loginResult = authService.login(loginRequest);

		return ResponseEntity.ok()
			.header("Set-Cookie", createRefreshTokenCookie(loginResult.refreshToken()).toString())
			.body(loginResult.loginResponse());
	}

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

	// 회원가입용 인증코드 발송 API입니다.
	// 로컬 환경에서는 인증코드가 콘솔 로그에 출력되고, 운영 환경에서는 이메일로 발송합니다.
	@PostMapping("/signup/code")
	public ResponseEntity<Void> sendSignupCode(
		@Valid @RequestBody EmailCodeSendRequest emailCodeSendRequest
	) {
		signupEmailCodeService.sendSignupCode(emailCodeSendRequest);

		return ResponseEntity.ok().build();
	}

	// 사용자가 입력한 인증코드가 Redis에 저장된 인증코드와 일치하는지 확인합니다.
	@PostMapping("/signup/code/verify")
	public ResponseEntity<Void> verifySignupCode(
		@Valid @RequestBody EmailCodeVerifyRequest emailCodeVerifyRequest
	) {
		signupEmailCodeService.verifySignupCode(emailCodeVerifyRequest);

		return ResponseEntity.ok().build();
	}

	// 이메일 인증 완료 여부를 확인한 뒤 최종 회원가입을 처리합니다.
	@PostMapping("/signup")
	public ResponseEntity<SignupResponse> signup(
		@Valid @RequestBody SignupRequest signupRequest
	) {
		SignupResponse signupResponse = authService.signup(signupRequest);

		return ResponseEntity.status(HttpStatus.CREATED).body(signupResponse);
	}

	private ResponseCookie createRefreshTokenCookie(String refreshToken) {
		return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
			.httpOnly(true)
			.secure(true)
			.sameSite("Lax")
			.path(REFRESH_TOKEN_COOKIE_PATH)
			.maxAge(REFRESH_TOKEN_COOKIE_MAX_AGE_SECONDS)
			.build();
	}
}
