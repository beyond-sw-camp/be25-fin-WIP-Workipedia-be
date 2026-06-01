package com.wip.workipedia.point.dto;

import java.time.LocalDateTime;

public record PointHistoryResponse(
	long pointHistoryId,
	int pointAmount,
	String reasonType,
	String relatedType,
	Long relatedId,
	LocalDateTime createdAt
) {
}
