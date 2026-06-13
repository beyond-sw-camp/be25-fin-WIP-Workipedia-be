package com.wip.workipedia.admin.commonqueue.service;

import com.wip.workipedia.admin.commonqueue.dto.CommonQueueAssignDepartmentRequest;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.department.repository.DepartmentRepository;
import com.wip.workipedia.notification.service.NotificationService;
import com.wip.workipedia.ticket.domain.Ticket;
import com.wip.workipedia.ticket.domain.TicketStatus;
import com.wip.workipedia.ticket.dto.RoutingResult;
import com.wip.workipedia.ticket.dto.TicketResponse;
import com.wip.workipedia.ticket.repository.TicketRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCommonQueueService {

	private final TicketRepository ticketRepository;
	private final DepartmentRepository departmentRepository;
	private final NotificationService notificationService;

	public PageResponse<TicketResponse> findCommonQueueTickets(Pageable pageable) {
		return PageResponse.from(
			ticketRepository.findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(TicketStatus.COMMON_QUEUE, pageable)
				.map(ticket -> TicketResponse.from(ticket, emptyRoutingResult()))
		);
	}

	@Transactional
	public TicketResponse assignDepartment(Long ticketId, CommonQueueAssignDepartmentRequest request) {
		Ticket ticket = ticketRepository.findActiveByTicketIdForUpdate(ticketId)
			.orElseThrow(() -> new CustomException(ErrorType.TICKET_NOT_FOUND));
		if (ticket.getStatus() != TicketStatus.COMMON_QUEUE) {
			throw new CustomException(ErrorType.TICKET_INVALID_ASSIGNMENT);
		}
		Department department = departmentRepository.findByDepartmentIdAndDeletedAtIsNull(request.departmentId())
			.orElseThrow(() -> new CustomException(ErrorType.DEPARTMENT_NOT_FOUND));

		ticket.assignDepartment(department.getDepartmentId());
		notificationService.createTicketNotification(ticket.getRequesterId(), ticket);
		return TicketResponse.from(ticket, new RoutingResult(
			department.getDepartmentId(),
			department.getDepartmentName(),
			ticket.getRoutingConfidenceScore(),
			ticket.getRoutingDecision(),
			List.of("Admin assigned the common queue ticket to the department."),
			List.of()
		));
	}

	private RoutingResult emptyRoutingResult() {
		return new RoutingResult(null, null, null, null, List.of(), List.of());
	}
}
