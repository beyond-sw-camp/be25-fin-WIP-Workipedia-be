package com.wip.workipedia.auth.service;

import com.wip.workipedia.auth.dto.SignupRequest;
import com.wip.workipedia.auth.dto.SignupResponse;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.department.repository.DepartmentRepository;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.repository.UserRepository;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private static final String[] NICKNAME_PREFIXES = {
		"연결하는", "성장하는", "학습하는", "공유하는", "기여하는",
		"도전하는", "지원하는", "발견하는", "개선하는", "소통하는"
	};

	private static final String[] NICKNAME_SUFFIXES = {
		"전략가", "멘토", "조력자", "개척자",
		"아키텍트", "빌더", "혁신가", "리더"
	};

	private final DepartmentRepository departmentRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailVerificationService emailVerificationService;
	private final SecureRandom secureRandom = new SecureRandom();

	@Transactional
	public SignupResponse signup(SignupRequest signupRequest) {
		if (!emailVerificationService.isSignupEmailVerified(signupRequest.email())) {
			throw new CustomException(ErrorType.AUTH_EMAIL_VERIFICATION_REQUIRED);
		}

		Department department = departmentRepository.findById(signupRequest.departmentId())
			.orElseThrow(() -> new CustomException(ErrorType.AUTH_DEPARTMENT_NOT_FOUND));

		if (userRepository.existsByEmployeeId(signupRequest.employeeId())) {
			throw new CustomException(ErrorType.AUTH_DUPLICATE_EMPLOYEE_ID);
		}

		if (userRepository.existsByEmail(signupRequest.email())) {
			throw new CustomException(ErrorType.AUTH_DUPLICATE_EMAIL);
		}

		String encodedPassword = passwordEncoder.encode(signupRequest.password());
		String nickname = generateNickname();
		User user = userRepository.save(User.signup(
			department,
			signupRequest.employeeId(),
			signupRequest.email(),
			encodedPassword,
			nickname
		));

		return new SignupResponse(
			user.getUserId(),
			user.getRole().name(),
			user.getNickname(),
			user.getStatus().name()
		);
	}

	private String generateNickname() {
		String prefix = NICKNAME_PREFIXES[secureRandom.nextInt(NICKNAME_PREFIXES.length)];
		String suffix = NICKNAME_SUFFIXES[secureRandom.nextInt(NICKNAME_SUFFIXES.length)];

		return prefix + suffix;
	}
}
