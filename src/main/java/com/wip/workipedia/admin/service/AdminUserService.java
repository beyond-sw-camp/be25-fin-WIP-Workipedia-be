package com.wip.workipedia.admin.service;

import com.wip.workipedia.admin.dto.AdminUserResponse;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.security.SecurityUtil;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.domain.UserStatus;
import com.wip.workipedia.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public AdminUserResponse search(String employeeId) {
		User user = userRepository.findByEmployeeId(employeeId)
			.orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다."));

		return AdminUserResponse.from(user);
	}

	@Transactional
	public AdminUserResponse changeStatus(Long userId, UserStatus status) {
		validateSelfInactivation(userId, status);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다."));

		user.changeStatus(status);

		return AdminUserResponse.from(user);
	}

	private void validateSelfInactivation(Long userId, UserStatus status) {
		if (status == UserStatus.INACTIVE && userId.equals(SecurityUtil.getCurrentUserId())) {
			throw new CustomException(ErrorType.FORBIDDEN, "자기 자신은 비활성화할 수 없습니다.");
		}
	}
}
