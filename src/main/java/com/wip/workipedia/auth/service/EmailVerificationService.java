package com.wip.workipedia.auth.service;

import java.time.Duration;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

// 회원가입 이메일 인증코드와 인증 완료 상태를 Redis에 저장하고 조회합니다.
// 인증코드는 임시 데이터이므로 TTL이 지나면 자동으로 만료됩니다.
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

	private static final String SIGNUP_EMAIL_CODE_KEY_PREFIX = "signup:email-code:";
	private static final String SIGNUP_EMAIL_VERIFIED_KEY_PREFIX = "signup:email-verified:";
	private static final String PASSWORD_RESET_EMAIL_CODE_KEY_PREFIX = "password-reset:email-code:";
	private static final String PASSWORD_RESET_EMAIL_VERIFIED_KEY_PREFIX = "password-reset:email-verified:";
	private static final String VERIFIED_VALUE = "true";
	private static final Duration SIGNUP_EMAIL_CODE_TTL = Duration.ofMinutes(5);
	private static final Duration SIGNUP_EMAIL_VERIFIED_TTL = Duration.ofMinutes(30);
	private static final Duration PASSWORD_RESET_EMAIL_CODE_TTL = Duration.ofMinutes(5);
	private static final Duration PASSWORD_RESET_EMAIL_VERIFIED_TTL = Duration.ofMinutes(30);

	private final StringRedisTemplate stringRedisTemplate;

	// 회원가입 인증 완료 여부를 조회합니다.
	public boolean isSignupEmailVerified(String email) {
		String verified = stringRedisTemplate.opsForValue().get(createSignupEmailVerifiedKey(email));

		return VERIFIED_VALUE.equals(verified);
	}

	// 비밀번호 재설정 인증 완료 여부를 조회합니다.
	public boolean isPasswordResetEmailVerified(
		String employeeId,
		String email
	) {
		String verified = stringRedisTemplate.opsForValue()
			.get(createPasswordResetEmailVerifiedKey(employeeId, email));

		return VERIFIED_VALUE.equals(verified);
	}

	// 회원가입 인증코드를 Redis에 저장합니다.
	public void saveSignupEmailCode(String email, String code) {
		stringRedisTemplate.opsForValue()
			.set(createSignupEmailCodeKey(email), code, SIGNUP_EMAIL_CODE_TTL);
	}

	// 비밀번호 재설정 인증코드를 Redis에 저장합니다.
	public void savePasswordResetEmailCode(
		String employeeId,
		String email,
		String code
	) {
		stringRedisTemplate.opsForValue()
			.set(createPasswordResetEmailCodeKey(employeeId, email), code, PASSWORD_RESET_EMAIL_CODE_TTL);
	}

	// 회원가입 인증코드가 Redis에 저장된 값과 일치하는지 확인합니다.
	public boolean matchesSignupEmailCode(String email, String code) {
		String savedCode = stringRedisTemplate.opsForValue().get(createSignupEmailCodeKey(email));

		return code.equals(savedCode);
	}

	// 비밀번호 재설정 인증코드가 Redis에 저장된 값과 일치하는지 확인합니다.
	public boolean matchesPasswordResetEmailCode(
		String employeeId,
		String email,
		String code
	) {
		String savedCode = stringRedisTemplate.opsForValue()
			.get(createPasswordResetEmailCodeKey(employeeId, email));

		return code.equals(savedCode);
	}

	// 회원가입 인증 완료 상태를 Redis에 저장합니다.
	public void markSignupEmailVerified(String email) {
		stringRedisTemplate.opsForValue()
			.set(createSignupEmailVerifiedKey(email), VERIFIED_VALUE, SIGNUP_EMAIL_VERIFIED_TTL);
	}

	// 비밀번호 재설정 인증 완료 상태를 Redis에 저장합니다.
	public void markPasswordResetEmailVerified(
		String employeeId,
		String email
	) {
		stringRedisTemplate.opsForValue()
			.set(
				createPasswordResetEmailVerifiedKey(employeeId, email),
				VERIFIED_VALUE,
				PASSWORD_RESET_EMAIL_VERIFIED_TTL
			);
	}

	// 사용 완료된 회원가입 인증코드를 Redis에서 삭제합니다.
	public void deleteSignupEmailCode(String email) {
		stringRedisTemplate.delete(createSignupEmailCodeKey(email));
	}

	// 사용 완료된 비밀번호 재설정 인증코드를 Redis에서 삭제합니다.
	public void deletePasswordResetEmailCode(
		String employeeId,
		String email
	) {
		stringRedisTemplate.delete(createPasswordResetEmailCodeKey(employeeId, email));
	}

	// 비밀번호 변경 완료 후 인증 완료 상태를 Redis에서 삭제합니다.
	public void deletePasswordResetEmailVerified(
		String employeeId,
		String email
	) {
		stringRedisTemplate.delete(createPasswordResetEmailVerifiedKey(employeeId, email));
	}

	private String createSignupEmailCodeKey(String email) {
		return SIGNUP_EMAIL_CODE_KEY_PREFIX + normalize(email);
	}

	private String createSignupEmailVerifiedKey(String email) {
		return SIGNUP_EMAIL_VERIFIED_KEY_PREFIX + normalize(email);
	}

	private String createPasswordResetEmailCodeKey(
		String employeeId,
		String email
	) {
		return PASSWORD_RESET_EMAIL_CODE_KEY_PREFIX + employeeId.trim() + ":" + normalize(email);
	}

	private String createPasswordResetEmailVerifiedKey(
		String employeeId,
		String email
	) {
		return PASSWORD_RESET_EMAIL_VERIFIED_KEY_PREFIX + employeeId.trim() + ":" + normalize(email);
	}

	private String normalize(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
