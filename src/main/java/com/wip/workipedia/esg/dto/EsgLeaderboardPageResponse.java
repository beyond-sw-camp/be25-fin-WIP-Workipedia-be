package com.wip.workipedia.esg.dto;

import java.util.List;

public record EsgLeaderboardPageResponse(
	List<EsgLeaderboardResponse> topRankers,
	EsgLeaderboardResponse myRank
) {
}
