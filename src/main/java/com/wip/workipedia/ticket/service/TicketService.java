package com.wip.workipedia.ticket.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.ticket.domain.Ticket;
import com.wip.workipedia.ticket.domain.TicketStatus;
import com.wip.workipedia.ticket.dto.CreateTicketRequest;
import com.wip.workipedia.ticket.dto.RoutingResult;
import com.wip.workipedia.ticket.dto.TicketAssigneeResponse;
import com.wip.workipedia.ticket.dto.TicketResponse;
import com.wip.workipedia.ticket.repository.TicketRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketService {
	private static final Long SKELETON_REQUESTER_ID = 1L;

	private final TicketRepository ticketRepository;
	private final TicketRoutingService ticketRoutingService;

	public TicketResponse create(CreateTicketRequest request) {
		Ticket ticket = Ticket.create(
			SKELETON_REQUESTER_ID,
			request.questionId(),
			request.sourceChatbotMessageId(),
			request.categoryId(),
			request.title(),
			request.content()
		);
		RoutingResult routingResult = ticketRoutingService.route(request);
		ticket.applyRouting(
			routingResult.assignedDepartmentId(),
			routingResult.assignedDepartmentName(),
			routingResult.confidenceScore(),
			routingResult.decision()
		);

		return TicketResponse.from(ticketRepository.save(ticket), routingResult);
	}

	public List<TicketResponse> findAll() {
		return ticketRepository.findAll(Sort.by(Sort.Direction.DESC, "ticketId")).stream()
			.map(ticket -> TicketResponse.from(ticket, emptyRoutingResult()))
			.toList();
	}

	public TicketResponse findById(Long ticketId) {
		Ticket ticket = getTicket(ticketId);
		return TicketResponse.from(ticket, emptyRoutingResult());
	}

	public TicketResponse changeStatus(Long ticketId, TicketStatus status) {
		Ticket ticket = getTicket(ticketId);
		ticket.changeStatus(status);
		return TicketResponse.from(ticketRepository.save(ticket), emptyRoutingResult());
	}

	public TicketAssigneeResponse assign(Long ticketId, Long assigneeId) {
		Ticket ticket = getTicket(ticketId);
		ticket.assignTo(assigneeId);
		ticketRepository.save(ticket);
		return new TicketAssigneeResponse(ticket.getTicketId(), ticket.getStatus(), ticket.getAssigneeId(), "노잇" + assigneeId);
	}

	private Ticket getTicket(Long ticketId) {
		return ticketRepository.findById(ticketId)
			.orElseThrow(() -> new CustomException(ErrorType.TICKET_NOT_FOUND));
	}

	private RoutingResult emptyRoutingResult() {
		return new RoutingResult(null, null, null, null, List.of(), List.of());
	}
}
