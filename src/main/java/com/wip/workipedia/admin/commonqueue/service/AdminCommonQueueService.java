package com.wip.workipedia.admin.commonqueue.service;

import com.wip.workipedia.admin.commonqueue.dto.CommonQueueAssignDepartmentRequest;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.department.repository.DepartmentRepository;
import com.wip.workipedia.notification.service.NotificationService;
import com.wip.workipedia.ticket.domain.Ticket;
import com.wip.workipedia.ticket.domain.TicketTransferRequestStatus;
import com.wip.workipedia.ticket.domain.TicketStatus;
import com.wip.workipedia.ticket.dto.RoutingResult;
import com.wip.workipedia.ticket.dto.TicketResponse;
import com.wip.workipedia.ticket.repository.TicketRepository;
import com.wip.workipedia.ticket.repository.TicketTransferRequestRepository;
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
	private final TicketTransferRequestRepository ticketTransferRequestRepository;

	public PageResponse<TicketResponse> findCommonQueueTickets(Pageable pageable) {
		return PageResponse.from(
			ticketRepository.findCommonQueueTickets(
					List.of(TicketStatus.COMMON_QUEUE, TicketStatus.TRANSFERRED),
					TicketTransferRequestStatus.REQUESTED,
					pageable
				)
				.map(this::toCommonQueueTicketResponse)
		);
	}

	@Transactional
	public TicketResponse assignDepartment(Long ticketId, CommonQueueAssignDepartmentRequest request) {
		Ticket ticket = ticketRepository.findActiveByTicketIdForUpdate(ticketId)
			.orElseThrow(() -> new CustomException(ErrorType.TICKET_NOT_FOUND));
		if (ticket.getStatus() != TicketStatus.COMMON_QUEUE && ticket.getStatus() != TicketStatus.TRANSFERRED) {
			throw new CustomException(ErrorType.TICKET_INVALID_ASSIGNMENT);
		}
		Department department = departmentRepository.findByDepartmentIdAndDeletedAtIsNull(request.departmentId())
			.orElseThrow(() -> new CustomException(ErrorType.DEPARTMENT_NOT_FOUND));
		boolean isReassignedTicket = ticketTransferRequestRepository
			.findFirstByTicketIdAndStatusAndDeletedAtIsNullOrderByCreatedAtDesc(
				ticketId,
				TicketTransferRequestStatus.REQUESTED
			)
			.map(transferRequest -> {
				transferRequest.markAssignedFromQueue();
				return true;
			})
			.orElse(false);

		ticket.assignDepartment(department.getDepartmentId());
		if (isReassignedTicket) {
			notificationService.createTicketReassignedNotification(ticket.getRequesterId(), ticket);
		} else {
			notificationService.createTicketNotification(ticket.getRequesterId(), ticket);
		}
		return TicketResponse.from(ticket, new RoutingResult(
			department.getDepartmentId(),
			department.getDepartmentName(),
			ticket.getRoutingConfidenceScore(),
			ticket.getRoutingDecision(),
			List.of("Admin assigned the common queue ticket to the department."),
			List.of()
		));
	}

	private TicketResponse toCommonQueueTicketResponse(TicketRepository.CommonQueueTicketProjection ticket) {
		return new TicketResponse(
			ticket.getTicketId(),
			ticket.getStatus(),
			ticket.getAssignedDepartmentId(),
			null,
			ticket.getRoutingConfidenceScore(),
			ticket.getRoutingDecision(),
			List.of(),
			List.of(),
			ticket.getSourceChatbotMessageId(),
			ticket.getPriority(),
			ticket.getTitle(),
			ticket.getContent(),
			ticket.getAssigneeId(),
			ticket.getTransferReason(),
			ticket.getTransferSuggestedDepartmentId(),
			ticket.getTransferSuggestedDepartmentName(),
			ticket.getCreatedAt(),
			ticket.getUpdatedAt()
		);
	}
}
