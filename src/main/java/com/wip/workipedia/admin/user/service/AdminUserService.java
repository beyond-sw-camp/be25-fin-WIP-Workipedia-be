package com.wip.workipedia.admin.user.service;

import com.wip.workipedia.admin.user.dto.AdminUserResponse;
import com.wip.workipedia.admin.domain.AdminLog;
import com.wip.workipedia.admin.repository.AdminLogRepository;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.common.security.SecurityUtil;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.domain.UserRole;
import com.wip.workipedia.user.domain.UserStatus;
import com.wip.workipedia.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

	private final UserRepository userRepository;
	private final AdminLogRepository adminLogRepository;

	@Transactional(readOnly = true)
	public PageResponse<AdminUserResponse> findAll(Pageable pageable) {
		return PageResponse.from(userRepository.findByDeletedAtIsNull(pageable).map(AdminUserResponse::from));
	}

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

	@Transactional
	public AdminUserResponse promoteToTeamAdmin(Long actorUserId, Long userId, UserRole role) {
		if (role != UserRole.TEAM_ADMIN) {
			throw new CustomException(ErrorType.BAD_REQUEST, "TEAM_ADMIN 권한으로만 변경할 수 있습니다.");
		}

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다."));

		validatePromotableToTeamAdmin(user);
		user.changeRole(UserRole.TEAM_ADMIN);
		adminLogRepository.save(AdminLog.of(
			actorUserId,
			"PROMOTE_TO_TEAM_ADMIN",
			"USER",
			"사용자를 팀관리자로 승격했습니다.",
			"{\"targetUserId\":" + user.getUserId() + ",\"role\":\"TEAM_ADMIN\"}"
		));

		return AdminUserResponse.from(user);
	}

	private void validateSelfInactivation(Long userId, UserStatus status) {
		if (status == UserStatus.INACTIVE && userId.equals(SecurityUtil.getCurrentUserId())) {
			throw new CustomException(ErrorType.FORBIDDEN, "자기 자신은 비활성화할 수 없습니다.");
		}
	}

	private void validatePromotableToTeamAdmin(User user) {
		if (user.getDeletedAt() != null) {
			throw new CustomException(ErrorType.FORBIDDEN, "삭제된 계정은 팀관리자로 승격할 수 없습니다.");
		}
		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new CustomException(ErrorType.FORBIDDEN, "비활성화된 계정은 팀관리자로 승격할 수 없습니다.");
		}
		if (user.getRole() == UserRole.TEAM_ADMIN) {
			throw new CustomException(ErrorType.CONFLICT, "이미 팀관리자인 사용자입니다.");
		}
		if (user.getRole() != UserRole.USER) {
			throw new CustomException(ErrorType.BAD_REQUEST, "일반 사용자만 팀관리자로 승격할 수 있습니다.");
		}
	}
}
