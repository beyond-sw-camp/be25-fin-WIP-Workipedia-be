package com.wip.workipedia.auth.service;

import com.wip.workipedia.auth.dto.PasswordResetCodeSendRequest;
import com.wip.workipedia.auth.dto.PasswordResetCodeVerifyRequest;
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

	// 비밀번호 재설정 대상 사용자 확인 후 인증코드를 이메일로 발송합니다.
	public void sendPasswordResetCode(PasswordResetCodeSendRequest passwordResetCodeSendRequest) {
		String employeeId = passwordResetCodeSendRequest.employeeId();
		String email = passwordResetCodeSendRequest.email();

		userRepository.findByEmployeeIdAndEmail(employeeId, email)
			.orElseThrow(() -> new CustomException(ErrorType.AUTH_USER_NOT_FOUND));

		String code = generateCode();
		emailSender.sendPasswordResetCode(email, code);
		emailVerificationService.savePasswordResetEmailCode(employeeId, email, code);
	}

	// 비밀번호 재설정 인증코드를 검증하고 인증 완료 상태를 저장합니다.
	public void verifyPasswordResetCode(PasswordResetCodeVerifyRequest passwordResetCodeVerifyRequest) {
		String employeeId = passwordResetCodeVerifyRequest.employeeId();
		String email = passwordResetCodeVerifyRequest.email();
		String code = passwordResetCodeVerifyRequest.code();

		userRepository.findByEmployeeIdAndEmail(employeeId, email)
			.orElseThrow(() -> new CustomException(ErrorType.AUTH_USER_NOT_FOUND));

		if (!emailVerificationService.matchesPasswordResetEmailCode(employeeId, email, code)) {
			throw new CustomException(ErrorType.AUTH_EMAIL_CODE_MISMATCH);
		}

		emailVerificationService.markPasswordResetEmailVerified(employeeId, email);
		emailVerificationService.deletePasswordResetEmailCode(employeeId, email);
	}

	// 6자리 숫자 인증코드를 생성합니다.
	private String generateCode() {
		return "%06d".formatted(secureRandom.nextInt(EMAIL_CODE_BOUND));
	}
}
