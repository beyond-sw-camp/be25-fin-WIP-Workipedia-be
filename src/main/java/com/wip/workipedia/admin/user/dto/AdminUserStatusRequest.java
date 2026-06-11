package com.wip.workipedia.admin.user.dto;

import com.wip.workipedia.user.domain.UserStatus;
import jakarta.validation.constraints.NotNull;

public record AdminUserStatusRequest(
	@NotNull(message = "변경할 사용자 상태를 입력해주세요.")
	UserStatus status
) {
}
