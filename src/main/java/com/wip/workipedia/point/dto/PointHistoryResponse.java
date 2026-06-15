package com.wip.workipedia.point.dto;

import com.wip.workipedia.point.domain.PointHistory;
import com.wip.workipedia.point.domain.PointHistoryType;
import java.time.LocalDateTime;

public record PointHistoryResponse(
	long pointHistoryId,
	int pointAmount,
	PointHistoryType type,
	String reasonType,
	String relatedType,
	Long relatedId,
	LocalDateTime createdAt
) {
	public static PointHistoryResponse from(PointHistory pointHistory) {
		return new PointHistoryResponse(
			pointHistory.getPointHistoryId(),
			pointHistory.getPointAmount(),
			pointHistory.getType(),
			pointHistory.getReasonType(),
			pointHistory.getRelatedType(),
			pointHistory.getRelatedId(),
			pointHistory.getCreatedAt()
		);
	}
}
