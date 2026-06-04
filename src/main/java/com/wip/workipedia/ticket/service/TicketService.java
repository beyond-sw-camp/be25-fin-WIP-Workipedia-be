package com.wip.workipedia.ticket.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.ticket.domain.Ticket;
import com.wip.workipedia.ticket.domain.TicketPriority;
import com.wip.workipedia.ticket.domain.TicketStatus;
import com.wip.workipedia.ticket.dto.CreateTicketRequest;
import com.wip.workipedia.ticket.dto.RoutingResult;
import com.wip.workipedia.ticket.dto.TicketAssigneeResponse;
import com.wip.workipedia.ticket.dto.TicketResponse;
import com.wip.workipedia.ticket.repository.TicketRepository;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TicketService {
	private static final Long SKELETON_REQUESTER_ID = 1L;

	private final TicketRepository ticketRepository;
	private final TicketRoutingService ticketRoutingService;
	private final UserRepository userRepository;

	@Transactional
	public TicketResponse create(CreateTicketRequest request) {
		Ticket ticket = Ticket.create(
			SKELETON_REQUESTER_ID,
			request.questionId(),
			request.sourceChatbotMessageId(),
			request.categoryId(),
			defaultPriority(request.priority()),
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

	@Transactional(readOnly = true)
	public PageResponse<TicketResponse> findAll(TicketStatus status, Long departmentId, Pageable pageable) {
		Page<Ticket> tickets = findTickets(status, departmentId, pageable);

		return PageResponse.from(
			tickets.map(ticket -> TicketResponse.from(ticket, emptyRoutingResult()))
		);
	}

	@Transactional(readOnly = true)
	public TicketResponse findById(Long ticketId) {
		Ticket ticket = getTicket(ticketId);
		return TicketResponse.from(ticket, emptyRoutingResult());
	}

	@Transactional
	public TicketResponse changeStatus(Long ticketId, TicketStatus status) {
		Ticket ticket = getTicket(ticketId);
		ticket.changeStatus(status);
		return TicketResponse.from(ticketRepository.save(ticket), emptyRoutingResult());
	}

	@Transactional
	public TicketAssigneeResponse assign(Long ticketId, Long assigneeId) {
		Ticket ticket = getTicket(ticketId);
		ticket.assignTo(assigneeId);
		ticketRepository.save(ticket);
		User assignee = userRepository.findById(assigneeId)
			.orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다. id=" + assigneeId));
		return new TicketAssigneeResponse(
			ticket.getTicketId(),
			ticket.getStatus(),
			ticket.getPriority(),
			ticket.getAssigneeId(),
			assignee.getNickname()
		);
	}

	private Ticket getTicket(Long ticketId) {
		return ticketRepository.findById(ticketId)
			.orElseThrow(() -> new CustomException(ErrorType.TICKET_NOT_FOUND));
	}

	private Page<Ticket> findTickets(TicketStatus status, Long departmentId, Pageable pageable) {
		if (status != null && departmentId != null) {
			return ticketRepository.findByStatusAndAssignedDepartmentId(status, departmentId, pageable);
		}
		if (status != null) {
			return ticketRepository.findByStatus(status, pageable);
		}
		if (departmentId != null) {
			return ticketRepository.findByAssignedDepartmentId(departmentId, pageable);
		}
		return ticketRepository.findAll(pageable);
	}

	private RoutingResult emptyRoutingResult() {
		return new RoutingResult(null, null, null, null, List.of(), List.of());
	}

	private TicketPriority defaultPriority(TicketPriority priority) {
		return priority == null ? TicketPriority.MEDIUM : priority;
	}
}
