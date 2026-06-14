package com.wip.workipedia.team.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.department.repository.DepartmentRepository;
import com.wip.workipedia.ticket.domain.RoutingDecision;
import com.wip.workipedia.ticket.domain.Ticket;
import com.wip.workipedia.ticket.domain.TicketPriority;
import com.wip.workipedia.ticket.domain.TicketStatus;
import com.wip.workipedia.ticket.dto.TicketTransferRequestCreateRequest;
import com.wip.workipedia.ticket.repository.TicketRepository;
import com.wip.workipedia.ticket.repository.TicketTransferRequestRepository;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.domain.UserRole;
import com.wip.workipedia.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TeamTicketServiceTest {

	@Mock
	private TicketRepository ticketRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private DepartmentRepository departmentRepository;

	@Mock
	private TicketTransferRequestRepository ticketTransferRequestRepository;

	@Test
	void getSummary_countsAssignedAndCompletedTicketsInOwnDepartmentForUser() {
		TeamTicketService service = service();
		User actor = user(UserRole.USER, 10L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
		when(ticketRepository.countVisibleByStatusInDepartment(any(), any(), any())).thenReturn(List.of(
			statusCount(TicketStatus.ASSIGNED, 2L),
			statusCount(TicketStatus.COMPLETED, 1L)
		));

		var response = service.getSummary(1L);

		assertThat(response.departmentId()).isEqualTo(10L);
		assertThat(response.totalCount()).isEqualTo(3L);
		assertThat(response.assignedCount()).isEqualTo(2L);
		assertThat(response.completedCount()).isEqualTo(1L);
	}

	@Test
	void findTicket_allowsTeamAdminInSameDepartment() {
		TeamTicketService service = service();
		User actor = user(UserRole.TEAM_ADMIN, 10L);
		Ticket ticket = assignedTicket(100L, 10L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
		when(ticketRepository.findByTicketIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(ticket));

		var response = service.findTicket(1L, 100L);

		assertThat(response.ticketId()).isEqualTo(100L);
		assertThat(response.assignedDepartmentId()).isEqualTo(10L);
	}

	@Test
	void findTicket_rejectsOtherDepartmentTicket() {
		TeamTicketService service = service();
		User actor = user(UserRole.USER, 10L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
		when(ticketRepository.findByTicketIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(assignedTicket(100L, 20L)));

		assertThatThrownBy(() -> service.findTicket(1L, 100L))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.TICKET_FORBIDDEN);
	}

	@Test
	void requestTransfer_movesAssignedTicketToTransferredAndStoresReason() {
		TeamTicketService service = service();
		User actor = user(UserRole.TEAM_ADMIN, 10L);
		Ticket ticket = assignedTicket(100L, 10L);
		Department suggestedDepartment = department(20L, "department-20");
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
		when(ticketRepository.findActiveByTicketIdForUpdate(100L)).thenReturn(Optional.of(ticket));
		when(departmentRepository.findByDepartmentIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(suggestedDepartment));

		var response = service.requestTransfer(
			1L,
			100L,
			new TicketTransferRequestCreateRequest("다른 부서 업무입니다.", 20L)
		);

		assertThat(response.status()).isEqualTo(TicketStatus.TRANSFERRED);
		assertThat(response.assignedDepartmentId()).isNull();
		assertThat(response.assigneeId()).isNull();
		assertThat(response.transferReason()).isEqualTo("다른 부서 업무입니다.");
		assertThat(response.transferSuggestedDepartmentId()).isEqualTo(20L);
		assertThat(response.transferSuggestedDepartmentName()).isEqualTo("department-20");
		assertThat(ticket.getStatus()).isEqualTo(TicketStatus.TRANSFERRED);
		assertThat(ticket.getAssignedDepartmentId()).isNull();
		verify(ticketTransferRequestRepository).save(argThat(request ->
			request.getTicketId().equals(100L)
				&& request.getRequesterId().equals(1L)
				&& request.getFromDepartmentId().equals(10L)
				&& request.getSuggestedDepartmentId().equals(20L)
				&& request.getReason().equals("다른 부서 업무입니다.")
		));
	}

	@Test
	void requestTransfer_rejectsNonTeamAdmin() {
		TeamTicketService service = service();
		User actor = user(UserRole.USER, 10L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));

		assertThatThrownBy(() -> service.requestTransfer(
			1L,
			100L,
			new TicketTransferRequestCreateRequest("다른 부서 업무입니다.", null)
		))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.TICKET_FORBIDDEN);
	}

	private TeamTicketService service() {
		return new TeamTicketService(
			ticketRepository,
			userRepository,
			departmentRepository,
			ticketTransferRequestRepository
		);
	}

	private Ticket assignedTicket(Long ticketId, Long departmentId) {
		Ticket ticket = Ticket.create(1L, null, TicketPriority.MEDIUM, "title", "content");
		ticket.applyRouting(departmentId, null, BigDecimal.valueOf(95, 2), RoutingDecision.AUTO_ASSIGNED);
		ticket.assignTo(2L);
		ReflectionTestUtils.setField(ticket, "ticketId", ticketId);
		return ticket;
	}

	private User user(UserRole role, Long departmentId) {
		User user = mock(User.class);
		Department department = department(departmentId, "department-" + departmentId);
		lenient().when(user.getUserId()).thenReturn(1L);
		lenient().when(user.getRole()).thenReturn(role);
		lenient().when(user.getDepartment()).thenReturn(department);
		return user;
	}

	private Department department(Long departmentId, String name) {
		Department department = mock(Department.class);
		lenient().when(department.getDepartmentId()).thenReturn(departmentId);
		lenient().when(department.getDepartmentName()).thenReturn(name);
		return department;
	}

	private TicketRepository.TicketStatusCountProjection statusCount(TicketStatus status, long count) {
		return new TicketRepository.TicketStatusCountProjection() {
			@Override
			public TicketStatus getStatus() {
				return status;
			}

			@Override
			public long getCount() {
				return count;
			}
		};
	}
}
