package com.wip.workipedia.auth.service;

import com.wip.workipedia.auth.dto.PasswordResetCodeSendRequest;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.user.repository.UserRepository;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordResetEmailCodeService {

	private static final int EMAIL_CODE_BOUND = 1_000_000;

	private final EmailVerificationService emailVerificationService;
	private final UserRepository userRepository;
	private final EmailSender emailSender;
	private final SecureRandom secureRandom = new SecureRandom();

	public void sendPasswordResetCode(PasswordResetCodeSendRequest passwordResetCodeSendRequest) {
		String employeeId = passwordResetCodeSendRequest.employeeId();
		String email = passwordResetCodeSendRequest.email();

		userRepository.findByEmployeeIdAndEmail(employeeId, email)
			.orElseThrow(() -> new CustomException(ErrorType.AUTH_USER_NOT_FOUND));

		String code = generateCode();
		emailSender.sendSignupCode(email, code);
		emailVerificationService.savePasswordResetEmailCode(employeeId, email, code);
	}

	private String generateCode() {
		return "%06d".formatted(secureRandom.nextInt(EMAIL_CODE_BOUND));
	}
}
