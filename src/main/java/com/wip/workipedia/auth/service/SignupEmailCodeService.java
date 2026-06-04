package com.wip.workipedia.auth.service;

import com.wip.workipedia.auth.dto.EmailCodeSendRequest;
import com.wip.workipedia.auth.dto.EmailCodeVerifyRequest;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.user.repository.UserRepository;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
// 회원가입 인증코드 생성, 발송 요청, Redis 검증 흐름을 담당합니다.
// 실제 발송 방식은 EmailSender 구현체가 담당하므로 로컬/운영 환경별로 교체할 수 있습니다.
public class SignupEmailCodeService {

	private static final int EMAIL_CODE_BOUND = 1_000_000;

	private final EmailVerificationService emailVerificationService;
	private final UserRepository userRepository;
	private final EmailSender emailSender;
	private final SecureRandom secureRandom = new SecureRandom();

	public void sendSignupCode(EmailCodeSendRequest emailCodeSendRequest) {
		String email = emailCodeSendRequest.email();

		if (userRepository.existsByEmail(email)) {
			throw new CustomException(ErrorType.AUTH_DUPLICATE_EMAIL);
		}

		String code = generateCode();
		emailSender.sendSignupCode(email, code);
		emailVerificationService.saveSignupEmailCode(email, code);
	}

	public void verifySignupCode(EmailCodeVerifyRequest emailCodeVerifyRequest) {
		if (!emailVerificationService.matchesSignupEmailCode(
			emailCodeVerifyRequest.email(),
			emailCodeVerifyRequest.code()
		)) {
			throw new CustomException(ErrorType.AUTH_EMAIL_CODE_MISMATCH);
		}

		emailVerificationService.markSignupEmailVerified(emailCodeVerifyRequest.email());
		emailVerificationService.deleteSignupEmailCode(emailCodeVerifyRequest.email());
	}

	private String generateCode() {
		return "%06d".formatted(secureRandom.nextInt(EMAIL_CODE_BOUND));
	}
}
