package com.wip.workipedia.point.dto;

import com.wip.workipedia.point.domain.PointHistory;
import java.time.LocalDateTime;

public record PointHistoryResponse(
	long pointHistoryId,
	int pointAmount,
	String reasonType,
	String relatedType,
	Long relatedId,
	LocalDateTime createdAt
) {
	public static PointHistoryResponse from(PointHistory pointHistory) {
		return new PointHistoryResponse(
			pointHistory.getPointHistoryId(),
			pointHistory.getPointAmount(),
			pointHistory.getReasonType(),
			pointHistory.getRelatedType(),
			pointHistory.getRelatedId(),
			pointHistory.getCreatedAt()
		);
	}
}
