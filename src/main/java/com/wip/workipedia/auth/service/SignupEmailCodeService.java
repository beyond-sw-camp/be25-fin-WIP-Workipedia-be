package com.wip.workipedia.auth.service;

import com.wip.workipedia.auth.dto.EmailCodeSendRequest;
import com.wip.workipedia.auth.dto.EmailCodeVerifyRequest;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.user.repository.UserRepository;
import java.security.SecureRandom;
import org.springframework.stereotype.Service;

@Service
public class SignupEmailCodeService {

	private static final int EMAIL_CODE_BOUND = 1_000_000;

	private final EmailVerificationService emailVerificationService;
	private final UserRepository userRepository;
	private final EmailSender emailSender;
	private final SecureRandom secureRandom = new SecureRandom();

	public SignupEmailCodeService(
		EmailVerificationService emailVerificationService,
		UserRepository userRepository,
		EmailSender emailSender
	) {
		this.emailVerificationService = emailVerificationService;
		this.userRepository = userRepository;
		this.emailSender = emailSender;
	}

	public void sendSignupCode(EmailCodeSendRequest emailCodeSendRequest) {
		String email = emailCodeSendRequest.email();

		if (userRepository.existsByEmail(email)) {
			throw new CustomException(ErrorType.CONFLICT, "이미 사용 중인 이메일입니다.");
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
			throw new CustomException(ErrorType.BAD_REQUEST, "인증코드가 일치하지 않습니다.");
		}

		emailVerificationService.markSignupEmailVerified(emailCodeVerifyRequest.email());
		emailVerificationService.deleteSignupEmailCode(emailCodeVerifyRequest.email());
	}

	private String generateCode() {
		return "%06d".formatted(secureRandom.nextInt(EMAIL_CODE_BOUND));
	}
}
