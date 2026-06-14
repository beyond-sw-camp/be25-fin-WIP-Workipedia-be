package com.wip.workipedia.admin.commonqueue.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wip.workipedia.admin.commonqueue.dto.CommonQueueAssignDepartmentRequest;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.department.repository.DepartmentRepository;
import com.wip.workipedia.notification.service.NotificationService;
import com.wip.workipedia.ticket.domain.Ticket;
import com.wip.workipedia.ticket.domain.TicketPriority;
import com.wip.workipedia.ticket.domain.TicketTransferRequest;
import com.wip.workipedia.ticket.domain.TicketTransferRequestStatus;
import com.wip.workipedia.ticket.domain.TicketStatus;
import com.wip.workipedia.ticket.repository.TicketRepository;
import com.wip.workipedia.ticket.repository.TicketTransferRequestRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminCommonQueueServiceTest {

	@Mock
	private TicketRepository ticketRepository;

	@Mock
	private DepartmentRepository departmentRepository;

	@Mock
	private NotificationService notificationService;

	@Mock
	private TicketTransferRequestRepository ticketTransferRequestRepository;

	@Test
	void assignDepartment_movesCommonQueueTicketToAssigned() {
		AdminCommonQueueService service = new AdminCommonQueueService(
			ticketRepository, departmentRepository, notificationService, ticketTransferRequestRepository);
		Ticket ticket = commonQueueTicket(100L);
		Department department = department(10L, "IT");
		when(ticketRepository.findActiveByTicketIdForUpdate(100L)).thenReturn(Optional.of(ticket));
		when(departmentRepository.findByDepartmentIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(department));
		when(ticketTransferRequestRepository.findFirstByTicketIdAndStatusAndDeletedAtIsNullOrderByCreatedAtDesc(
			100L,
			TicketTransferRequestStatus.REQUESTED
		)).thenReturn(Optional.empty());

		var response = service.assignDepartment(100L, new CommonQueueAssignDepartmentRequest(10L));

		assertThat(response.status()).isEqualTo(TicketStatus.ASSIGNED);
		assertThat(response.assignedDepartmentId()).isEqualTo(10L);
		assertThat(ticket.getStatus()).isEqualTo(TicketStatus.ASSIGNED);
		assertThat(ticket.getAssignedDepartmentId()).isEqualTo(10L);
		assertThat(ticket.getAssigneeId()).isNull();
		assertThat(ticket.getAssignedAt()).isNotNull();
		verify(notificationService).createTicketNotification(ticket.getRequesterId(), ticket);
	}

	@Test
	void assignDepartment_rejectsNonCommonQueueTicket() {
		AdminCommonQueueService service = new AdminCommonQueueService(
			ticketRepository, departmentRepository, notificationService, ticketTransferRequestRepository);
		Ticket ticket = commonQueueTicket(100L);
		ticket.assignDepartment(10L);
		when(ticketRepository.findActiveByTicketIdForUpdate(100L)).thenReturn(Optional.of(ticket));

		assertThatThrownBy(() -> service.assignDepartment(100L, new CommonQueueAssignDepartmentRequest(10L)))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.TICKET_INVALID_ASSIGNMENT);
	}

	@Test
	void assignDepartment_movesTransferredTicketToAssignedAndClosesTransferRequest() {
		AdminCommonQueueService service = new AdminCommonQueueService(
			ticketRepository, departmentRepository, notificationService, ticketTransferRequestRepository);
		Ticket ticket = commonQueueTicket(100L);
		ticket.assignDepartment(20L);
		ticket.requestTransfer();
		Department department = department(10L, "IT");
		TicketTransferRequest transferRequest = TicketTransferRequest.create(
			100L,
			1L,
			20L,
			10L,
			"다른 부서 업무입니다."
		);
		when(ticketRepository.findActiveByTicketIdForUpdate(100L)).thenReturn(Optional.of(ticket));
		when(departmentRepository.findByDepartmentIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(department));
		when(ticketTransferRequestRepository.findFirstByTicketIdAndStatusAndDeletedAtIsNullOrderByCreatedAtDesc(
			100L,
			TicketTransferRequestStatus.REQUESTED
		)).thenReturn(Optional.of(transferRequest));

		var response = service.assignDepartment(100L, new CommonQueueAssignDepartmentRequest(10L));

		assertThat(response.status()).isEqualTo(TicketStatus.ASSIGNED);
		assertThat(ticket.getStatus()).isEqualTo(TicketStatus.ASSIGNED);
		assertThat(transferRequest.getStatus()).isEqualTo(TicketTransferRequestStatus.ASSIGNED_FROM_QUEUE);
		verify(notificationService).createTicketReassignedNotification(ticket.getRequesterId(), ticket);
	}

	private Ticket commonQueueTicket(Long ticketId) {
		Ticket ticket = Ticket.create(99L, null, TicketPriority.MEDIUM, "title", "content");
		ReflectionTestUtils.setField(ticket, "ticketId", ticketId);
		return ticket;
	}

	private Department department(Long departmentId, String name) {
		Department department = mock(Department.class);
		when(department.getDepartmentId()).thenReturn(departmentId);
		when(department.getDepartmentName()).thenReturn(name);
		return department;
	}
}
