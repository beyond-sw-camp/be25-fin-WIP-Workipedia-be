package com.wip.workipedia.admin.commonqueue.dto;

import jakarta.validation.constraints.NotNull;

public record CommonQueueAssignDepartmentRequest(
	@NotNull Long departmentId
) {
}
