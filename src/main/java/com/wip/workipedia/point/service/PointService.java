package com.wip.workipedia.point.service;

import com.wip.workipedia.point.dto.MyPointResponse;
import com.wip.workipedia.point.dto.PointHistoryResponse;
import com.wip.workipedia.point.dto.PointRankingResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PointService {

	public MyPointResponse getMyPoint() {
		return new MyPointResponse(1L, 120L, 45L, 3);
	}

	public List<PointHistoryResponse> getMyPointHistory() {
		return List.of(
			new PointHistoryResponse(1L, 10, "QUESTION_CREATED", "WORKI_QUESTION", 1L, LocalDateTime.now().minusDays(1)),
			new PointHistoryResponse(2L, 30, "ANSWER_ACCEPTED", "WORKI_ANSWER", 5L, LocalDateTime.now().minusHours(3))
		);
	}

	public List<PointRankingResponse> getRanking() {
		return List.of(
			new PointRankingResponse(1, 7L, "노잇1042", 240L),
			new PointRankingResponse(2, 3L, "노잇3391", 180L),
			new PointRankingResponse(3, 1L, "노잇1234", 120L)
		);
	}
}
