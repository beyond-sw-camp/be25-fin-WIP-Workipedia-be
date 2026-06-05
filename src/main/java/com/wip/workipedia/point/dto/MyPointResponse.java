package com.wip.workipedia.point.dto;

import com.wip.workipedia.point.domain.UserPoint;

public record MyPointResponse(
	long userId,
	long currentPoint
) {
	public static MyPointResponse from(UserPoint userPoint) {
		return new MyPointResponse(userPoint.getUserId(), userPoint.getCurrentPoint());
	}
}
