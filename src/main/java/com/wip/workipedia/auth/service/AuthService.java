package com.wip.workipedia.auth.service;

import com.wip.workipedia.auth.dto.LoginRequest;
import com.wip.workipedia.auth.dto.LoginResponse;
import com.wip.workipedia.auth.dto.LoginResult;
import com.wip.workipedia.auth.dto.PasswordResetRequest;
import com.wip.workipedia.auth.dto.SignupRequest;
import com.wip.workipedia.auth.dto.SignupResponse;
import com.wip.workipedia.auth.dto.TokenRefreshResult;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.security.JwtProvider;
import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.department.repository.DepartmentRepository;
import com.wip.workipedia.point.service.PointService;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.domain.UserStatus;
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
	private final RefreshTokenService refreshTokenService;
	private final JwtProvider jwtProvider;
	private final PointService pointService;
	private final SecureRandom secureRandom = new SecureRandom();

	// 로그인 성공 시 Access Token과 Refresh Token을 발급합니다.
	@Transactional
	public LoginResult login(LoginRequest loginRequest) {
		User user = userRepository.findByEmployeeId(loginRequest.employeeId())
			.orElseThrow(() -> new CustomException(ErrorType.AUTH_INVALID_CREDENTIALS));

		if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
			throw new CustomException(ErrorType.AUTH_INVALID_CREDENTIALS);
		}

		validateActiveUser(user);

		String accessToken = jwtProvider.createAccessToken(user);
		String refreshToken = jwtProvider.createRefreshToken(user);
		refreshTokenService.save(user.getUserId(), refreshToken);
		user.updateLastLoginAt();
		// 로그인 성공 후 로그인 포인트 적립을 처리합니다.
		// 실제 중복 지급 여부와 일일 적립 한도는 PointService에서 검증합니다.
		pointService.earnLoginPoint(user.getUserId());

		return new LoginResult(
			createLoginResponse(user, accessToken),
			refreshToken
		);
	}

	// Refresh Token을 검증한 뒤 Access Token과 Refresh Token을 재발급합니다.
	@Transactional
	public TokenRefreshResult refreshToken(String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank()) {
			throw new CustomException(ErrorType.AUTH_REFRESH_TOKEN_REQUIRED);
		}

		if (!jwtProvider.isValidRefreshToken(refreshToken)) {
			throw new CustomException(ErrorType.AUTH_REFRESH_TOKEN_INVALID);
		}

		Long userId = jwtProvider.getUserId(refreshToken);
		if (!refreshTokenService.matches(userId, refreshToken)) {
			throw new CustomException(ErrorType.AUTH_REFRESH_TOKEN_INVALID);
		}

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorType.AUTH_REFRESH_TOKEN_INVALID));
		validateActiveUser(user);

		String newAccessToken = jwtProvider.createAccessToken(user);
		String newRefreshToken = jwtProvider.createRefreshToken(user);
		refreshTokenService.save(user.getUserId(), newRefreshToken);

		return new TokenRefreshResult(newAccessToken, newRefreshToken);
	}

	// 로그아웃 시 userId 기준으로 Redis에 저장된 Refresh Token을 삭제합니다.
	public void logout(Long userId) {
		refreshTokenService.delete(userId);
	}

	// 비밀번호 재설정 인증 완료 여부를 확인한 뒤 새 비밀번호로 변경합니다.
	@Transactional
	public void resetPassword(PasswordResetRequest passwordResetRequest) {
		String employeeId = passwordResetRequest.employeeId();
		String email = passwordResetRequest.email();

		if (!emailVerificationService.isPasswordResetEmailVerified(employeeId, email)) {
			throw new CustomException(ErrorType.AUTH_PASSWORD_RESET_VERIFICATION_REQUIRED);
		}

		User user = userRepository.findByEmployeeIdAndEmail(employeeId, email)
			.orElseThrow(() -> new CustomException(ErrorType.AUTH_USER_NOT_FOUND));

		String encodedPassword = passwordEncoder.encode(passwordResetRequest.newPassword());
		user.updatePassword(encodedPassword);
		refreshTokenService.delete(user.getUserId());
		emailVerificationService.deletePasswordResetEmailVerified(employeeId, email);
	}

	// 이메일 인증 완료 여부를 확인한 뒤 회원가입을 처리합니다.
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

	private void validateActiveUser(User user) {
		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new CustomException(ErrorType.AUTH_INACTIVE_USER);
		}
	}

	private LoginResponse createLoginResponse(
		User user,
		String accessToken
	) {
		return new LoginResponse(
			accessToken,
			user.getUserId(),
			user.getDepartment().getDepartmentId(),
			user.getDepartment().getDepartmentName(),
			user.getRole().name(),
			user.getNickname(),
			user.getStatus().name()
		);
	}
}
