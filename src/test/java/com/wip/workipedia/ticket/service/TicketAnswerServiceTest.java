package com.wip.workipedia.ticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.notification.service.NotificationService;
import com.wip.workipedia.point.domain.PointReasonType;
import com.wip.workipedia.point.service.PointService;
import com.wip.workipedia.storage.dto.StoredObjectMetadata;
import com.wip.workipedia.storage.service.StorageService;
import com.wip.workipedia.ticket.domain.RoutingDecision;
import com.wip.workipedia.ticket.domain.Ticket;
import com.wip.workipedia.ticket.domain.TicketAnswer;
import com.wip.workipedia.ticket.domain.TicketPriority;
import com.wip.workipedia.ticket.domain.TicketStatus;
import com.wip.workipedia.ticket.dto.TicketAnswerCreateRequest;
import com.wip.workipedia.ticket.repository.TicketAnswerRepository;
import com.wip.workipedia.ticket.repository.TicketRepository;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TicketAnswerServiceTest {

	@Mock
	private TicketRepository ticketRepository;

	@Mock
	private TicketAnswerRepository ticketAnswerRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private NotificationService notificationService;

	@Mock
	private StorageService storageService;

	@Mock
	private PointService pointService;

	@Test
	void createOfficialAnswer_savesAnswerAndCompletesTicket() {
		TicketAnswerService service = new TicketAnswerService(
			ticketRepository, ticketAnswerRepository, userRepository, notificationService, storageService, pointService);
		User actor = user(1L, 10L);
		Ticket ticket = assignedTicket(100L, 10L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
		when(ticketRepository.findActiveByTicketIdForUpdate(100L)).thenReturn(Optional.of(ticket));
		when(ticketAnswerRepository.save(org.mockito.ArgumentMatchers.any(TicketAnswer.class)))
			.thenAnswer(invocation -> {
				TicketAnswer answer = invocation.getArgument(0);
				ReflectionTestUtils.setField(answer, "ticketAnswerId", 500L);
				return answer;
			});

		var response = service.createOfficialAnswer(1L, 100L, new TicketAnswerCreateRequest(" 처리 완료했습니다. "));

		assertThat(response.answerId()).isEqualTo(500L);
		assertThat(response.content()).isEqualTo("처리 완료했습니다.");
		assertThat(ticket.getStatus()).isEqualTo(TicketStatus.COMPLETED);
		assertThat(ticket.getCompletedAt()).isNotNull();
		verify(pointService).earnPoint(
			eq(1L),
			eq(15),
			eq(PointReasonType.TICKET_ANSWER_CREATED),
			eq("TICKET_ANSWER"),
			eq(500L)
		);
		verify(notificationService).createTicketNotification(ticket.getRequesterId(), ticket);
	}

	@Test
	void createOfficialAnswer_rejectsOtherDepartmentUser() {
		TicketAnswerService service = new TicketAnswerService(
			ticketRepository, ticketAnswerRepository, userRepository, notificationService, storageService, pointService);
		User actor = user(1L, 20L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
		when(ticketRepository.findActiveByTicketIdForUpdate(100L)).thenReturn(Optional.of(assignedTicket(100L, 10L)));

		assertThatThrownBy(() -> service.createOfficialAnswer(1L, 100L, new TicketAnswerCreateRequest("답변")))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.TICKET_FORBIDDEN);
	}

	@Test
	void createOfficialAnswer_rejectsUserWithoutDepartment() {
		TicketAnswerService service = new TicketAnswerService(
			ticketRepository, ticketAnswerRepository, userRepository, notificationService, storageService, pointService);
		User actor = mock(User.class);
		when(actor.getDepartment()).thenReturn(null);
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
		when(ticketRepository.findActiveByTicketIdForUpdate(100L)).thenReturn(Optional.of(assignedTicket(100L, 10L)));

		assertThatThrownBy(() -> service.createOfficialAnswer(1L, 100L, new TicketAnswerCreateRequest("답변")))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.TICKET_FORBIDDEN);
	}

	@Test
	void createOfficialAnswer_rejectsCompletedTicket() {
		TicketAnswerService service = new TicketAnswerService(
			ticketRepository, ticketAnswerRepository, userRepository, notificationService, storageService, pointService);
		Ticket ticket = assignedTicket(100L, 10L);
		ticket.complete();
		User actor = user(1L, 10L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
		when(ticketRepository.findActiveByTicketIdForUpdate(100L)).thenReturn(Optional.of(ticket));

		assertThatThrownBy(() -> service.createOfficialAnswer(1L, 100L, new TicketAnswerCreateRequest("답변")))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.TICKET_INVALID_ANSWER);
	}

	@Test
	void createOfficialAnswer_resolvesAttachmentMetadataFromStorage() {
		TicketAnswerService service = new TicketAnswerService(
			ticketRepository, ticketAnswerRepository, userRepository, notificationService, storageService, pointService);
		User actor = user(1L, 10L);
		Ticket ticket = assignedTicket(100L, 10L);
		String objectKey = "tickets/replies/uuid/guide.pdf";
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
		when(ticketRepository.findActiveByTicketIdForUpdate(100L)).thenReturn(Optional.of(ticket));
		when(storageService.getObjectMetadata(objectKey)).thenReturn(
			new StoredObjectMetadata(objectKey, "https://r2.example.com/" + objectKey, "guide.pdf", "application/pdf", 1234L)
		);
		when(ticketAnswerRepository.save(org.mockito.ArgumentMatchers.any(TicketAnswer.class)))
			.thenAnswer(invocation -> {
				TicketAnswer answer = invocation.getArgument(0);
				ReflectionTestUtils.setField(answer, "ticketAnswerId", 500L);
				return answer;
			});

		var response = service.createOfficialAnswer(1L, 100L, new TicketAnswerCreateRequest("답변", objectKey));

		assertThat(response.fileKey()).isEqualTo(objectKey);
		assertThat(response.fileUrl()).isEqualTo("https://r2.example.com/" + objectKey);
		assertThat(response.fileName()).isEqualTo("guide.pdf");
		assertThat(response.fileContentType()).isEqualTo("application/pdf");
		assertThat(response.fileSize()).isEqualTo(1234L);
	}

	@Test
	void createOfficialAnswer_rejectsAttachmentOutsideTicketReplyPrefix() {
		TicketAnswerService service = new TicketAnswerService(
			ticketRepository, ticketAnswerRepository, userRepository, notificationService, storageService, pointService);
		User actor = user(1L, 10L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
		when(ticketRepository.findActiveByTicketIdForUpdate(100L)).thenReturn(Optional.of(assignedTicket(100L, 10L)));

		assertThatThrownBy(() -> service.createOfficialAnswer(1L, 100L, new TicketAnswerCreateRequest("답변", "manuals/uuid/file.pdf")))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.TICKET_INVALID_ATTACHMENT);
	}

	private Ticket assignedTicket(Long ticketId, Long departmentId) {
		Ticket ticket = Ticket.create(99L, null, TicketPriority.MEDIUM, "title", "content");
		ticket.applyRouting(departmentId, null, BigDecimal.valueOf(95, 2), RoutingDecision.AUTO_ASSIGNED);
		ReflectionTestUtils.setField(ticket, "ticketId", ticketId);
		return ticket;
	}

	private User user(Long userId, Long departmentId) {
		User user = mock(User.class);
		Department department = mock(Department.class);
		lenient().when(user.getUserId()).thenReturn(userId);
		when(user.getDepartment()).thenReturn(department);
		lenient().when(user.getNickname()).thenReturn("author");
		when(department.getDepartmentId()).thenReturn(departmentId);
		lenient().when(department.getDepartmentName()).thenReturn("department-" + departmentId);
		return user;
	}
}
