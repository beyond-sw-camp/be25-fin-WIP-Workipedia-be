package com.wip.workipedia.point.dto;

import com.wip.workipedia.point.domain.PointHistoryType;

public enum PointHistorySearchType {
	ALL,
	EARN,
	SPEND,
	RESET;

	public PointHistoryType toHistoryType() {
		return switch (this) {
			case EARN -> PointHistoryType.EARN;
			case SPEND -> PointHistoryType.SPEND;
			case RESET -> PointHistoryType.RESET;
			case ALL -> throw new IllegalStateException("ALL does not map to a single point history type");
		};
	}
}
