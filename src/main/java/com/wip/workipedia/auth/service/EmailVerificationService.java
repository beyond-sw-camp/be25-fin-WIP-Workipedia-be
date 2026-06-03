package com.wip.workipedia.auth.service;

import java.time.Duration;
import java.util.Locale;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class EmailVerificationService {

	private static final String SIGNUP_EMAIL_CODE_KEY_PREFIX = "signup:email-code:";
	private static final String SIGNUP_EMAIL_VERIFIED_KEY_PREFIX = "signup:email-verified:";
	private static final String VERIFIED_VALUE = "true";
	private static final Duration SIGNUP_EMAIL_CODE_TTL = Duration.ofMinutes(5);
	private static final Duration SIGNUP_EMAIL_VERIFIED_TTL = Duration.ofMinutes(30);

	private final StringRedisTemplate stringRedisTemplate;

	public EmailVerificationService(StringRedisTemplate stringRedisTemplate) {
		this.stringRedisTemplate = stringRedisTemplate;
	}

	public boolean isSignupEmailVerified(String email) {
		String verified = stringRedisTemplate.opsForValue().get(createSignupEmailVerifiedKey(email));

		return VERIFIED_VALUE.equals(verified);
	}

	public void saveSignupEmailCode(String email, String code) {
		stringRedisTemplate.opsForValue()
			.set(createSignupEmailCodeKey(email), code, SIGNUP_EMAIL_CODE_TTL);
	}

	public boolean matchesSignupEmailCode(String email, String code) {
		String savedCode = stringRedisTemplate.opsForValue().get(createSignupEmailCodeKey(email));

		return code.equals(savedCode);
	}

	public void markSignupEmailVerified(String email) {
		stringRedisTemplate.opsForValue()
			.set(createSignupEmailVerifiedKey(email), VERIFIED_VALUE, SIGNUP_EMAIL_VERIFIED_TTL);
	}

	public void deleteSignupEmailCode(String email) {
		stringRedisTemplate.delete(createSignupEmailCodeKey(email));
	}

	private String createSignupEmailCodeKey(String email) {
		return SIGNUP_EMAIL_CODE_KEY_PREFIX + normalize(email);
	}

	private String createSignupEmailVerifiedKey(String email) {
		return SIGNUP_EMAIL_VERIFIED_KEY_PREFIX + normalize(email);
	}

	private String normalize(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
