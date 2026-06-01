package com.wip.workipedia.admin.dto;

import java.time.LocalDateTime;

public record AdminLogResponse(
	long adminLogId,
	long actorId,
	String actionType,
	String targetType,
	Long targetId,
	String description,
	LocalDateTime createdAt
) {
}
