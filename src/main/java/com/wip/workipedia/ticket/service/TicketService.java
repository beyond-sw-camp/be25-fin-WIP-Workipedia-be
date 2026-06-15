package com.wip.workipedia.ticket.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.notification.service.NotificationService;
import com.wip.workipedia.ticket.domain.Ticket;
import com.wip.workipedia.ticket.domain.TicketPriority;
import com.wip.workipedia.ticket.domain.TicketRoutingLog;
import com.wip.workipedia.ticket.domain.TicketStatus;
import com.wip.workipedia.ticket.dto.CreateTicketRequest;
import com.wip.workipedia.ticket.dto.RoutingResult;
import com.wip.workipedia.ticket.dto.TicketAssigneeResponse;
import com.wip.workipedia.ticket.dto.TicketResponse;
import com.wip.workipedia.ticket.repository.TicketRepository;
import com.wip.workipedia.ticket.repository.TicketRoutingLogRepository;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {
	private final TicketRepository ticketRepository;
	private final TicketRoutingService ticketRoutingService;
	private final TicketRoutingLogRepository ticketRoutingLogRepository;
	private final UserRepository userRepository;
	private final NotificationService notificationService;
	private final ObjectMapper objectMapper;

	public TicketResponse create(Long requesterId, CreateTicketRequest request) {
		RoutingResult routingResult = ticketRoutingService.route(request);
		return saveTicket(requesterId, request, routingResult);
	}

	@Transactional
	public TicketResponse saveTicket(Long requesterId, CreateTicketRequest request, RoutingResult routingResult) {
		Ticket ticket = Ticket.create(
			requesterId,
			request.sourceChatbotMessageId(),
			defaultPriority(request.priority()),
			request.title(),
			request.content());
		ticket.applyRouting(
			routingResult.assignedDepartmentId(),
			routingResult.assignedDepartmentName(),
			routingResult.confidenceScore(),
			routingResult.decision());
		Ticket saved = ticketRepository.save(ticket);
		saveRoutingLog(saved, routingResult);
		notificationService.createTicketNotification(requesterId, saved);
		return TicketResponse.from(saved, routingResult);
	}

	@Transactional(readOnly = true)
	public PageResponse<TicketResponse> findMyTeamTickets(Long requesterId, TicketStatus status, Pageable pageable) {
		User requester = userRepository.findById(requesterId)
			.orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다. id=" + requesterId));
		Long departmentId = requester.getDepartment().getDepartmentId();
		Page<Ticket> tickets = findTickets(status, departmentId, pageable);
		return PageResponse.from(tickets.map(ticket -> TicketResponse.from(ticket, emptyRoutingResult())));
	}

	@Transactional(readOnly = true)
	public TicketResponse findById(Long ticketId) {
		return TicketResponse.from(getTicket(ticketId), emptyRoutingResult());
	}

	@Transactional
	public TicketResponse changeStatus(Long ticketId, TicketStatus status) {
		Ticket ticket = getTicket(ticketId);
		TicketStatus previousStatus = ticket.getStatus();
		ticket.changeStatus(status);
		Ticket saved = ticketRepository.save(ticket);
		if (previousStatus != status) {
			notificationService.createTicketNotification(saved.getRequesterId(), saved);
		}
		return TicketResponse.from(saved, emptyRoutingResult());
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
			assignee.getNickname());
	}

	@Transactional
	public void moveExpiredTicketsToCommonQueue() {
		ticketRepository.moveExpiredTicketsToCommonQueue();
		ticketRepository.softDeleteExpiredCommonQueueTickets();
	}

	private void saveRoutingLog(Ticket ticket, RoutingResult result) {
		TicketRoutingLog routingLog = TicketRoutingLog.create(
			ticket.getTicketId(),
			result.decision(),
			result.confidenceScore(),
			result.scoreMargin(),
			toJson(result.candidateDepartments()),
			toJson(result.reasons()),
			result.modelVersion()
		);
		ticketRoutingLogRepository.save(routingLog);
	}

	private String toJson(Object value) {
		if (value == null) {
			return null;
		}
		try {
			return objectMapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			log.warn("JSON 직렬화 실패: {}", e.getMessage());
			return null;
		}
	}

	private Ticket getTicket(Long ticketId) {
		return ticketRepository.findById(ticketId)
			.orElseThrow(() -> new CustomException(ErrorType.TICKET_NOT_FOUND));
	}

	private Page<Ticket> findTickets(TicketStatus status, Long departmentId, Pageable pageable) {
		if (status != null && departmentId != null) {
			return ticketRepository.findByStatusAndAssignedDepartmentIdAndDeletedAtIsNull(status, departmentId, pageable);
		}
		if (status != null) {
			return ticketRepository.findByStatusAndDeletedAtIsNull(status, pageable);
		}
		if (departmentId != null) {
			return ticketRepository.findByAssignedDepartmentIdAndDeletedAtIsNull(departmentId, pageable);
		}
		return ticketRepository.findByDeletedAtIsNull(pageable);
	}

	private RoutingResult emptyRoutingResult() {
		return new RoutingResult(null, null, null, null, null, null, List.of(), List.of());
	}

	private TicketPriority defaultPriority(TicketPriority priority) {
		return priority == null ? TicketPriority.MEDIUM : priority;
	}
}
