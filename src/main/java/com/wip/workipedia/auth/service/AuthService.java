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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private static final String[] NICKNAME_PREFIXES = {
		"차분한", "든든한", "민첩한", "따뜻한", "꼼꼼한"
	};

	private static final String[] NICKNAME_SUFFIXES = {
		"데이지", "라일락", "민들레", "해바라기", "수국"
	};

	private final DepartmentRepository departmentRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final SecureRandom secureRandom = new SecureRandom();

	public AuthService(
		DepartmentRepository departmentRepository,
		UserRepository userRepository,
		PasswordEncoder passwordEncoder
	) {
		this.departmentRepository = departmentRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public SignupResponse signup(SignupRequest signupRequest) {
		Department department = departmentRepository.findById(signupRequest.departmentId())
			.orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "부서를 찾을 수 없습니다."));

		if (userRepository.existsByEmployeeId(signupRequest.employeeId())) {
			throw new CustomException(ErrorType.CONFLICT, "이미 사용 중인 사번입니다.");
		}

		if (userRepository.existsByEmail(signupRequest.email())) {
			throw new CustomException(ErrorType.CONFLICT, "이미 사용 중인 이메일입니다.");
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
		int number = secureRandom.nextInt(10_000);

		return "%s%s%04d".formatted(prefix, suffix, number);
	}
}
