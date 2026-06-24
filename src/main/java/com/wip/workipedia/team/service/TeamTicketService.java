package com.wip.workipedia.team.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.team.dto.TeamTicketSummaryResponse;
import com.wip.workipedia.ticket.domain.Ticket;
import com.wip.workipedia.ticket.domain.TicketTransferRequest;
import com.wip.workipedia.ticket.domain.TicketTransferRequestStatus;
import com.wip.workipedia.ticket.domain.TicketStatus;
import com.wip.workipedia.ticket.dto.TicketTransferRequestCreateRequest;
import com.wip.workipedia.ticket.dto.RoutingResult;
import com.wip.workipedia.ticket.dto.TicketFileResponse;
import com.wip.workipedia.ticket.dto.TicketResponse;
import com.wip.workipedia.ticket.repository.TicketFileRepository;
import com.wip.workipedia.ticket.repository.TicketRepository;
import com.wip.workipedia.ticket.repository.TicketTransferRequestRepository;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.domain.UserRole;
import com.wip.workipedia.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamTicketService {

	private static final List<TicketStatus> SUMMARY_STATUSES = List.of(
		TicketStatus.ASSIGNED,
		TicketStatus.COMPLETED
	);
	private static final int COMPLETED_TICKET_VISIBLE_HOURS = 48;

	private final TicketRepository ticketRepository;
	private final TicketFileRepository ticketFileRepository;
	private final UserRepository userRepository;
	private final TicketTransferRequestRepository ticketTransferRequestRepository;

	public TeamTicketSummaryResponse getSummary(Long actorUserId) {
		User actor = getTeamMember(actorUserId);
		Department department = actor.getDepartment();
		Long departmentId = department.getDepartmentId();
		Map<TicketStatus, Long> counts = countByStatus(departmentId);
		long assignedCount = counts.getOrDefault(TicketStatus.ASSIGNED, 0L);
		long completedCount = counts.getOrDefault(TicketStatus.COMPLETED, 0L);

		return new TeamTicketSummaryResponse(
			departmentId,
			department.getDepartmentName(),
			assignedCount + completedCount,
			assignedCount,
			completedCount
		);
	}

	public PageResponse<TicketResponse> findTickets(Long actorUserId, TicketStatus status, Pageable pageable) {
		User actor = getTeamMember(actorUserId);
		Long departmentId = actor.getDepartment().getDepartmentId();
		Page<Ticket> tickets = ticketRepository.findVisibleTeamTickets(
			departmentId,
			status,
			completedVisibleAfter(),
			pageable
		);
		Map<Long, List<TicketFileResponse>> filesByTicketId = findFilesByTicketId(tickets.getContent());
		return PageResponse.from(tickets.map(ticket ->
			TicketResponse.from(ticket, emptyRoutingResult(), filesByTicketId.getOrDefault(ticket.getTicketId(), List.of()))));
	}

	public TicketResponse findTicket(Long actorUserId, Long ticketId) {
		User actor = getTeamMember(actorUserId);
		Ticket ticket = ticketRepository.findByTicketIdAndDeletedAtIsNull(ticketId)
			.orElseThrow(() -> new CustomException(ErrorType.TICKET_NOT_FOUND));
		assertDepartmentTicket(actor, ticket);
		return TicketResponse.from(ticket, emptyRoutingResult(), findFileResponses(ticket.getTicketId()));
	}

	@Transactional
	public TicketResponse requestTransfer(
		Long actorUserId,
		Long ticketId,
		TicketTransferRequestCreateRequest request
	) {
		User actor = getTeamMember(actorUserId);
		assertTeamAdmin(actor);
		Ticket ticket = ticketRepository.findActiveByTicketIdForUpdate(ticketId)
			.orElseThrow(() -> new CustomException(ErrorType.TICKET_NOT_FOUND));
		assertDepartmentTicket(actor, ticket);
		if (ticket.getStatus() != TicketStatus.ASSIGNED) {
			throw new CustomException(ErrorType.TICKET_INVALID_TRANSFER);
		}
		if (ticketTransferRequestRepository.existsByTicketIdAndStatusAndDeletedAtIsNull(
			ticketId,
			TicketTransferRequestStatus.REQUESTED
		)) {
			throw new CustomException(ErrorType.TICKET_INVALID_TRANSFER);
		}

		TicketTransferRequest transferRequest = TicketTransferRequest.create(
			ticket.getTicketId(),
			actorUserId,
			actor.getDepartment().getDepartmentId(),
			request.reason()
		);
		try {
			ticketTransferRequestRepository.saveAndFlush(transferRequest);
		} catch (DataIntegrityViolationException e) {
			throw new CustomException(ErrorType.TICKET_INVALID_TRANSFER);
		}
		ticket.requestTransfer();

		return TicketResponse.from(ticket, emptyRoutingResult())
			.withTransferInfo(request.reason());
	}

	private List<TicketFileResponse> findFileResponses(Long ticketId) {
		return ticketFileRepository.findByTicketIdAndDeletedAtIsNullOrderBySortOrderAsc(ticketId)
			.stream()
			.map(TicketFileResponse::from)
			.toList();
	}

	private Map<Long, List<TicketFileResponse>> findFilesByTicketId(List<Ticket> tickets) {
		if (tickets.isEmpty()) {
			return Map.of();
		}
		List<Long> ticketIds = tickets.stream()
			.map(Ticket::getTicketId)
			.toList();
		return ticketFileRepository.findByTicketIdInAndDeletedAtIsNullOrderByTicketIdAscSortOrderAsc(ticketIds)
			.stream()
			.collect(java.util.stream.Collectors.groupingBy(
				com.wip.workipedia.ticket.domain.TicketFile::getTicketId,
				java.util.stream.Collectors.mapping(TicketFileResponse::from, java.util.stream.Collectors.toList())
			));
	}

	private User getTeamMember(Long actorUserId) {
		User user = userRepository.findById(actorUserId)
			.orElseThrow(() -> new CustomException(ErrorType.TICKET_FORBIDDEN));
		if (user.getDepartment() == null) {
			throw new CustomException(ErrorType.TICKET_FORBIDDEN);
		}
		return user;
	}

	private void assertDepartmentTicket(User actor, Ticket ticket) {
		Long actorDepartmentId = actor.getDepartment().getDepartmentId();
		if (!actorDepartmentId.equals(ticket.getAssignedDepartmentId())) {
			throw new CustomException(ErrorType.TICKET_FORBIDDEN);
		}
	}

	private void assertTeamAdmin(User actor) {
		if (actor.getRole() != UserRole.TEAM_ADMIN) {
			throw new CustomException(ErrorType.TICKET_FORBIDDEN);
		}
	}

	private Map<TicketStatus, Long> countByStatus(Long departmentId) {
		Map<TicketStatus, Long> counts = new EnumMap<>(TicketStatus.class);
		ticketRepository.countVisibleByStatusInDepartment(departmentId, SUMMARY_STATUSES, completedVisibleAfter())
			.forEach(projection -> counts.put(projection.getStatus(), projection.getCount()));
		return counts;
	}

	private LocalDateTime completedVisibleAfter() {
		return LocalDateTime.now().minusHours(COMPLETED_TICKET_VISIBLE_HOURS);
	}

	private RoutingResult emptyRoutingResult() {
		return new RoutingResult(null, null, null, null, null, null, List.of(), List.of());
	}
}
