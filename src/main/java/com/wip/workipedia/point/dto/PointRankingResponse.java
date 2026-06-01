package com.wip.workipedia.point.dto;

public record PointRankingResponse(
	int rank,
	long userId,
	String nickname,
	long currentPoint
) {
}
