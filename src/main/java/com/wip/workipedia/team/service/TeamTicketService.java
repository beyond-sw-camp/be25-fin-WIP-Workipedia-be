package com.wip.workipedia.team.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.team.dto.TeamTicketSummaryResponse;
import com.wip.workipedia.ticket.domain.Ticket;
import com.wip.workipedia.ticket.domain.TicketStatus;
import com.wip.workipedia.ticket.dto.RoutingResult;
import com.wip.workipedia.ticket.dto.TicketResponse;
import com.wip.workipedia.ticket.repository.TicketAnswerRepository;
import com.wip.workipedia.ticket.repository.TicketRepository;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
	private final TicketAnswerRepository ticketAnswerRepository;
	private final UserRepository userRepository;

	public TeamTicketSummaryResponse getSummary(Long actorUserId) {
		User actor = getTeamMember(actorUserId);
		Department department = actor.getDepartment();
		Long departmentId = department.getDepartmentId();
		Map<TicketStatus, Long> counts = countByStatus(departmentId);
		long assignedCount = counts.getOrDefault(TicketStatus.ASSIGNED, 0L);
		long completedCount = counts.getOrDefault(TicketStatus.COMPLETED, 0L);
		long yearlyAssignedCount = ticketRepository.countByAssignedDepartmentIdAndAssignedAtGreaterThanEqualAndAssignedAtLessThanAndDeletedAtIsNull(
			departmentId,
			startOfCurrentYear(),
			startOfNextYear()
		);
		long myVisibleAnsweredCount = ticketAnswerRepository.countVisibleAnsweredTicketsByAuthorInDepartment(
			actor.getUserId(),
			departmentId
		);

		return new TeamTicketSummaryResponse(
			departmentId,
			department.getDepartmentName(),
			yearlyAssignedCount,
			myVisibleAnsweredCount,
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
		return PageResponse.from(tickets.map(ticket -> TicketResponse.from(ticket, emptyRoutingResult())));
	}

	public TicketResponse findTicket(Long actorUserId, Long ticketId) {
		User actor = getTeamMember(actorUserId);
		Ticket ticket = ticketRepository.findByTicketIdAndDeletedAtIsNull(ticketId)
			.orElseThrow(() -> new CustomException(ErrorType.TICKET_NOT_FOUND));
		assertDepartmentTicket(actor, ticket);
		return TicketResponse.from(ticket, emptyRoutingResult());
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

	private Map<TicketStatus, Long> countByStatus(Long departmentId) {
		Map<TicketStatus, Long> counts = new EnumMap<>(TicketStatus.class);
		ticketRepository.countVisibleByStatusInDepartment(departmentId, SUMMARY_STATUSES, completedVisibleAfter())
			.forEach(projection -> counts.put(projection.getStatus(), projection.getCount()));
		return counts;
	}

	private LocalDateTime completedVisibleAfter() {
		return LocalDateTime.now().minusHours(COMPLETED_TICKET_VISIBLE_HOURS);
	}

	private LocalDateTime startOfCurrentYear() {
		return LocalDate.now().withDayOfYear(1).atStartOfDay();
	}

	private LocalDateTime startOfNextYear() {
		return LocalDate.now().plusYears(1).withDayOfYear(1).atStartOfDay();
	}

	private RoutingResult emptyRoutingResult() {
		return new RoutingResult(null, null, null, null, List.of(), List.of());
	}
}
