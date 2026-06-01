package com.wip.workipedia.point.dto;

public record MyPointResponse(
	long userId,
	long currentPoint,
	long esgScore
) {
}
