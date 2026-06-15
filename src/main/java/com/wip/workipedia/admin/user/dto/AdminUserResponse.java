package com.wip.workipedia.admin.user.dto;

import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.user.domain.User;
import java.time.LocalDateTime;

public record AdminUserResponse(
	Long userId,
	String employeeId,
	String nickname,
	String role,
	String status,
	Long departmentId,
	String departmentName,
	LocalDateTime lastLoginAt
) {
	public static AdminUserResponse from(User user) {
		Department department = user.getDepartment();

		return new AdminUserResponse(
			user.getUserId(),
			user.getEmployeeId(),
			user.getNickname(),
			user.getRole().name(),
			user.getStatus().name(),
			department.getDepartmentId(),
			department.getDepartmentName(),
			user.getLastLoginAt()
		);
	}
}
