package com.wip.workipedia.ticket.ai;

import com.wip.workipedia.ticket.domain.RoutingDecision;
import com.wip.workipedia.ticket.dto.RoutingResult;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FallbackTicketRoutingAiClient implements TicketRoutingAiClient {

	@Override
	public RoutingResult recommend(TicketRoutingPrompt prompt) {
		return new RoutingResult(
			null,
			null,
			null,
			RoutingDecision.COMMON_QUEUE,
			List.of("AI 라우팅 구현체가 아직 연결되지 않아 공통 접수 큐로 이동"),
			List.of()
		);
	}
}
