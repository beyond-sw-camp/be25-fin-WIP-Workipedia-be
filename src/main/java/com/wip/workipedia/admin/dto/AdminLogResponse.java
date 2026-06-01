package com.wip.workipedia.admin.dto;

import java.time.LocalDateTime;

// 관리자 작업 로그 목록에 표시할 응답입니다.
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
