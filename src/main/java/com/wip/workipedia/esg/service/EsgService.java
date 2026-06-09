package com.wip.workipedia.esg.service;

import com.wip.workipedia.esg.dto.EsgLeaderboardPageResponse;
import com.wip.workipedia.esg.dto.EsgResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EsgService {

	public EsgResponse getMyEsg(Long userId) {
		return new EsgResponse(userId, 0, null, null, null, null);
	}

	public EsgLeaderboardPageResponse getLeaderBoardEsg() {
		return new EsgLeaderboardPageResponse(List.of(), null);
	}
}
