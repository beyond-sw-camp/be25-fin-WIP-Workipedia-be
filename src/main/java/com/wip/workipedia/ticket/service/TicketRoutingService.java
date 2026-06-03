package com.wip.workipedia.ticket.service;

import com.wip.workipedia.ticket.ai.TicketRoutingAiClient;
import com.wip.workipedia.ticket.ai.TicketRoutingPrompt;
import com.wip.workipedia.ticket.dto.CreateTicketRequest;
import com.wip.workipedia.ticket.dto.RoutingResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketRoutingService {
	private final TicketRoutingAiClient ticketRoutingAiClient;

	public RoutingResult route(CreateTicketRequest request) {
		TicketRoutingPrompt prompt = new TicketRoutingPrompt(
			request.title(),
			request.content(),
			request.categoryId(),
			request.questionId(),
			request.sourceChatbotMessageId()
		);

		return ticketRoutingAiClient.recommend(prompt);
	}
}
