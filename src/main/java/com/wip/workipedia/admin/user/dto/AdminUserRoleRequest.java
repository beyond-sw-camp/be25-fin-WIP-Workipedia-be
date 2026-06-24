package com.wip.workipedia.admin.user.dto;

import com.wip.workipedia.user.domain.UserRole;
import jakarta.validation.constraints.NotNull;

public record AdminUserRoleRequest(
	@NotNull(message = "변경할 사용자 권한을 입력해주세요.")
	UserRole role
) {
}
