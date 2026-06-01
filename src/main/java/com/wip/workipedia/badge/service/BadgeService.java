package com.wip.workipedia.badge.service;

import com.wip.workipedia.badge.dto.BadgeResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BadgeService {

	public List<BadgeResponse> getMyBadges() {
		return List.of(
			new BadgeResponse(1L, "FIRST_QUESTION", "첫 질문", "첫 워키 질문을 등록했습니다.", true, LocalDateTime.now().minusDays(2)),
			new BadgeResponse(2L, "FIRST_ACCEPTED_ANSWER", "첫 채택 답변", "처음으로 답변이 채택됐습니다.", false, null),
			new BadgeResponse(3L, "ANSWER_HELPER", "답변 도우미", "답변을 5개 이상 작성했습니다.", false, null)
		);
	}

	public List<BadgeResponse> getBadges() {
		return List.of(
			new BadgeResponse(1L, "FIRST_QUESTION", "첫 질문", "첫 워키 질문을 등록하면 지급됩니다.", false, null),
			new BadgeResponse(2L, "FIRST_ACCEPTED_ANSWER", "첫 채택 답변", "첫 채택 답변을 받으면 지급됩니다.", false, null),
			new BadgeResponse(3L, "ANSWER_HELPER", "답변 도우미", "답변을 5개 이상 작성하면 지급됩니다.", false, null)
		);
	}
}
