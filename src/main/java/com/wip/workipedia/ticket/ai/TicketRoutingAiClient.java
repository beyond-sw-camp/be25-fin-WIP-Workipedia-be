package com.wip.workipedia.ticket.ai;

import com.wip.workipedia.ticket.dto.RoutingResult;

public interface TicketRoutingAiClient {
	RoutingResult recommend(TicketRoutingPrompt prompt);
}
