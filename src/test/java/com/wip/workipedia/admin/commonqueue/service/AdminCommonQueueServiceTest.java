package com.wip.workipedia.admin.commonqueue.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.wip.workipedia.admin.commonqueue.dto.CommonQueueAssignDepartmentRequest;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.department.repository.DepartmentRepository;
import com.wip.workipedia.notification.service.NotificationService;
import com.wip.workipedia.ticket.domain.CommonQueueReason;
import com.wip.workipedia.ticket.domain.RoutingDecision;
import com.wip.workipedia.ticket.domain.Ticket;
import com.wip.workipedia.ticket.domain.TicketPriority;
import com.wip.workipedia.ticket.domain.TicketStatus;
import com.wip.workipedia.ticket.domain.TicketTransferRequest;
import com.wip.workipedia.ticket.domain.TicketTransferRequestStatus;
import com.wip.workipedia.ticket.repository.TicketRepository;
import com.wip.workipedia.ticket.repository.TicketTransferRequestRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
	void findCommonQueueTickets_usesProjectionWithoutAdditionalLookups() {
		AdminCommonQueueService service = service();
		PageRequest pageable = PageRequest.of(0, 20);
		LocalDateTime createdAt = LocalDateTime.of(2026, 6, 15, 9, 0);
		LocalDateTime updatedAt = LocalDateTime.of(2026, 6, 15, 9, 10);
		TicketRepository.CommonQueueTicketProjection projection = commonQueueProjection(
			100L,
			TicketStatus.COMMON_QUEUE,
			CommonQueueReason.TRANSFER_REQUESTED,
			"transfer reason",
			createdAt,
			updatedAt
		);
		when(ticketRepository.findCommonQueueTickets(
			TicketTransferRequestStatus.REQUESTED,
			pageable
		)).thenReturn(new PageImpl<>(List.of(projection), pageable, 1));

		var response = service.findCommonQueueTickets(pageable);

		assertThat(response.content()).hasSize(1);
		assertThat(response.content().getFirst().ticketId()).isEqualTo(100L);
		assertThat(response.content().getFirst().status()).isEqualTo(TicketStatus.COMMON_QUEUE);
		assertThat(response.content().getFirst().commonQueueReason()).isEqualTo(CommonQueueReason.TRANSFER_REQUESTED);
		assertThat(response.content().getFirst().commonQueueEnteredAt()).isEqualTo(createdAt);
		assertThat(response.content().getFirst().transferReason()).isEqualTo("transfer reason");
		assertThat(response.content().getFirst().createdAt()).isEqualTo(createdAt);
		assertThat(response.content().getFirst().updatedAt()).isEqualTo(updatedAt);
		verifyNoInteractions(departmentRepository, ticketTransferRequestRepository);
	}

	@Test
	void assignDepartment_movesCommonQueueTicketToAssigned() {
		AdminCommonQueueService service = service();
		Ticket ticket = commonQueueTicket(100L);
		Department department = department(10L, "IT");
		when(ticketRepository.findActiveByTicketIdForUpdate(100L)).thenReturn(Optional.of(ticket));
		when(departmentRepository.findActiveDepartmentById(10L)).thenReturn(Optional.of(department));
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
	void assignDepartment_rejectsMissingTicket() {
		AdminCommonQueueService service = service();
		when(ticketRepository.findActiveByTicketIdForUpdate(100L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.assignDepartment(100L, new CommonQueueAssignDepartmentRequest(10L)))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.TICKET_NOT_FOUND);
		verifyNoInteractions(departmentRepository, notificationService, ticketTransferRequestRepository);
	}

	@Test
	void assignDepartment_rejectsNonCommonQueueTicket() {
		AdminCommonQueueService service = service();
		Ticket ticket = commonQueueTicket(100L);
		ticket.assignDepartment(10L);
		when(ticketRepository.findActiveByTicketIdForUpdate(100L)).thenReturn(Optional.of(ticket));

		assertThatThrownBy(() -> service.assignDepartment(100L, new CommonQueueAssignDepartmentRequest(10L)))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.TICKET_INVALID_ASSIGNMENT);
		verify(departmentRepository, never()).findActiveDepartmentById(10L);
		verifyNoInteractions(notificationService, ticketTransferRequestRepository);
	}

	@Test
	void assignDepartment_rejectsDeletedOrInactiveDepartment() {
		AdminCommonQueueService service = service();
		Ticket ticket = commonQueueTicket(100L);
		when(ticketRepository.findActiveByTicketIdForUpdate(100L)).thenReturn(Optional.of(ticket));
		when(departmentRepository.findActiveDepartmentById(10L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.assignDepartment(100L, new CommonQueueAssignDepartmentRequest(10L)))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.DEPARTMENT_NOT_FOUND);
		assertThat(ticket.getStatus()).isEqualTo(TicketStatus.COMMON_QUEUE);
		verifyNoInteractions(notificationService, ticketTransferRequestRepository);
	}

	@Test
	void assignDepartment_movesTransferRequestedTicketToAssignedAndClosesTransferRequest() {
		AdminCommonQueueService service = service();
		Ticket ticket = commonQueueTicket(100L);
		ticket.assignDepartment(20L);
		ticket.requestTransfer();
		Department department = department(10L, "IT");
		TicketTransferRequest transferRequest = TicketTransferRequest.create(
			100L,
			1L,
			20L,
			"transfer reason"
		);
		when(ticketRepository.findActiveByTicketIdForUpdate(100L)).thenReturn(Optional.of(ticket));
		when(departmentRepository.findActiveDepartmentById(10L)).thenReturn(Optional.of(department));
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

	private AdminCommonQueueService service() {
		return new AdminCommonQueueService(
			ticketRepository,
			departmentRepository,
			notificationService,
			ticketTransferRequestRepository
		);
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

	private TicketRepository.CommonQueueTicketProjection commonQueueProjection(
		Long ticketId,
		TicketStatus status,
		CommonQueueReason commonQueueReason,
		String transferReason,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
	) {
		return new TicketRepository.CommonQueueTicketProjection() {
			@Override
			public Long getTicketId() {
				return ticketId;
			}

			@Override
			public TicketStatus getStatus() {
				return status;
			}

			@Override
			public Long getAssignedDepartmentId() {
				return null;
			}

			@Override
			public BigDecimal getRoutingConfidenceScore() {
				return BigDecimal.valueOf(0.75);
			}

			@Override
			public RoutingDecision getRoutingDecision() {
				return RoutingDecision.COMMON_QUEUE;
			}

			@Override
			public Long getSourceChatbotMessageId() {
				return null;
			}

			@Override
			public TicketPriority getPriority() {
				return TicketPriority.MEDIUM;
			}

			@Override
			public String getTitle() {
				return "title";
			}

			@Override
			public String getContent() {
				return "content";
			}

			@Override
			public Long getAssigneeId() {
				return null;
			}

			@Override
			public CommonQueueReason getCommonQueueReason() {
				return commonQueueReason;
			}

			@Override
			public LocalDateTime getCommonQueueEnteredAt() {
				return createdAt;
			}

			@Override
			public String getTransferReason() {
				return transferReason;
			}

			@Override
			public LocalDateTime getCreatedAt() {
				return createdAt;
			}

			@Override
			public LocalDateTime getUpdatedAt() {
				return updatedAt;
			}
		};
	}
}
