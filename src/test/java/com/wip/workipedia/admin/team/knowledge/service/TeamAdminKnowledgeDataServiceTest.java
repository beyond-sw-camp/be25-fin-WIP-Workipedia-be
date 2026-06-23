package com.wip.workipedia.admin.team.knowledge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wip.workipedia.admin.team.knowledge.dto.KnowledgeDataApprovalRequest;
import com.wip.workipedia.admin.team.knowledge.dto.KnowledgeDataUpdateRequest;
import com.wip.workipedia.aisync.service.AiSyncJobService;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.knowledge.domain.KnowledgeData;
import com.wip.workipedia.knowledge.repository.KnowledgeDataRepository;
import com.wip.workipedia.point.domain.PointReasonType;
import com.wip.workipedia.point.service.PointService;
import com.wip.workipedia.ticket.domain.KnowledgeReviewStatus;
import com.wip.workipedia.ticket.domain.RoutingDecision;
import com.wip.workipedia.ticket.domain.Ticket;
import com.wip.workipedia.ticket.domain.TicketAnswer;
import com.wip.workipedia.ticket.domain.TicketPriority;
import com.wip.workipedia.ticket.repository.TicketAnswerRepository;
import com.wip.workipedia.ticket.repository.TicketRepository;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.domain.UserRole;
import com.wip.workipedia.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TeamAdminKnowledgeDataServiceTest {

	@Mock
	private KnowledgeDataRepository knowledgeDataRepository;

	@Mock
	private TicketRepository ticketRepository;

	@Mock
	private TicketAnswerRepository ticketAnswerRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private PointService pointService;

	@Mock
	private AiSyncJobService aiSyncJobService;

	@Test
	void approve_createsKnowledgeDataFromCompletedTicketAnswer() {
		TeamAdminKnowledgeDataService service = service();
		User actor = user(1L, UserRole.TEAM_ADMIN, 10L);
		Ticket ticket = completedTicket(100L, 10L);
		TicketAnswer answer = answer(200L, 100L, "answer");
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
		when(ticketRepository.findActiveByTicketIdForUpdate(100L)).thenReturn(Optional.of(ticket));
		when(knowledgeDataRepository.existsByTicketId(100L)).thenReturn(false);
		when(ticketAnswerRepository.findTopByTicketIdAndDeletedAtIsNullOrderByCreatedAtDesc(100L))
			.thenReturn(Optional.of(answer));
		when(knowledgeDataRepository.save(any(KnowledgeData.class))).thenAnswer(invocation -> {
			KnowledgeData knowledgeData = invocation.getArgument(0);
			ReflectionTestUtils.setField(knowledgeData, "knowledgeDataId", 300L);
			return knowledgeData;
		});

		var response = service.approve(1L, 100L, new KnowledgeDataApprovalRequest("edited question", "edited answer"));

		assertThat(response.knowledgeDataId()).isEqualTo(300L);
		assertThat(response.ticketId()).isEqualTo(100L);
		assertThat(response.departmentId()).isEqualTo(10L);
		assertThat(response.question()).isEqualTo("edited question");
		assertThat(response.answer()).isEqualTo("edited answer");
		assertThat(ticket.getKnowledgeReviewStatus()).isEqualTo(KnowledgeReviewStatus.APPROVED);
		verify(pointService).earnPoint(
			eq(1L),
			eq(30),
			eq(PointReasonType.TICKET_KNOWLEDGE_CREATED),
			eq("KNOWLEDGE_DATA"),
			eq(300L)
		);
	}

	@Test
	void reject_marksCompletedTicketAsKnowledgeRejected() {
		TeamAdminKnowledgeDataService service = service();
		User actor = user(1L, UserRole.TEAM_ADMIN, 10L);
		Ticket ticket = completedTicket(100L, 10L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
		when(ticketRepository.findActiveByTicketIdForUpdate(100L)).thenReturn(Optional.of(ticket));
		when(knowledgeDataRepository.existsByTicketId(100L)).thenReturn(false);

		service.reject(1L, 100L);

		assertThat(ticket.getKnowledgeReviewStatus()).isEqualTo(KnowledgeReviewStatus.REJECTED);
		assertThat(ticket.getStatus().name()).isEqualTo("COMPLETED");
	}

	@Test
	void approve_rejectsNonTeamAdmin() {
		TeamAdminKnowledgeDataService service = service();
		User actor = user(1L, UserRole.USER, 10L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));

		assertThatThrownBy(() -> service.approve(1L, 100L, new KnowledgeDataApprovalRequest("question", "answer")))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.KNOWLEDGE_DATA_FORBIDDEN);
	}

	@Test
	void approve_rejectsOtherDepartmentTicket() {
		TeamAdminKnowledgeDataService service = service();
		User actor = user(1L, UserRole.TEAM_ADMIN, 10L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
		when(ticketRepository.findActiveByTicketIdForUpdate(100L)).thenReturn(Optional.of(completedTicket(100L, 20L)));

		assertThatThrownBy(() -> service.approve(1L, 100L, new KnowledgeDataApprovalRequest("question", "answer")))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.KNOWLEDGE_DATA_FORBIDDEN);
	}

	@Test
	void approve_rejectsNonCompletedTicket() {
		TeamAdminKnowledgeDataService service = service();
		User actor = user(1L, UserRole.TEAM_ADMIN, 10L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
		when(ticketRepository.findActiveByTicketIdForUpdate(100L)).thenReturn(Optional.of(assignedTicket(100L, 10L)));

		assertThatThrownBy(() -> service.approve(1L, 100L, new KnowledgeDataApprovalRequest("question", "answer")))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.KNOWLEDGE_DATA_INVALID_APPROVAL);
	}

	@Test
	void approve_rejectsAlreadyApprovedTicket() {
		TeamAdminKnowledgeDataService service = service();
		User actor = user(1L, UserRole.TEAM_ADMIN, 10L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
		when(ticketRepository.findActiveByTicketIdForUpdate(100L)).thenReturn(Optional.of(completedTicket(100L, 10L)));
		when(knowledgeDataRepository.existsByTicketId(100L)).thenReturn(true);

		assertThatThrownBy(() -> service.approve(1L, 100L, new KnowledgeDataApprovalRequest("question", "answer")))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.KNOWLEDGE_DATA_ALREADY_APPROVED);
	}

	@Test
	void update_changesQuestionAndAnswerInOwnDepartment() {
		TeamAdminKnowledgeDataService service = service();
		User actor = user(1L, UserRole.TEAM_ADMIN, 10L);
		KnowledgeData knowledgeData = knowledgeData(300L, 100L, 10L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
		when(knowledgeDataRepository.findByKnowledgeDataIdAndDeletedAtIsNull(300L)).thenReturn(Optional.of(knowledgeData));

		var response = service.update(1L, 300L, new KnowledgeDataUpdateRequest(" new question ", " new answer "));

		assertThat(response.question()).isEqualTo("new question");
		assertThat(response.answer()).isEqualTo("new answer");
	}

	@Test
	void delete_softDeletesKnowledgeDataInOwnDepartment() {
		TeamAdminKnowledgeDataService service = service();
		User actor = user(1L, UserRole.TEAM_ADMIN, 10L);
		KnowledgeData knowledgeData = knowledgeData(300L, 100L, 10L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
		when(knowledgeDataRepository.findByKnowledgeDataIdAndDeletedAtIsNull(300L)).thenReturn(Optional.of(knowledgeData));

		service.delete(1L, 300L);

		assertThat(knowledgeData.getDeletedAt()).isNotNull();
		assertThat(knowledgeData.getIsDeleted()).isEqualTo("Y");
	}

	@Test
	void delete_allowsSystemAdminForAnyDepartmentKnowledgeData() {
		TeamAdminKnowledgeDataService service = service();
		User actor = user(1L, UserRole.SYSTEM_ADMIN, null);
		KnowledgeData knowledgeData = knowledgeData(300L, 100L, 20L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
		when(knowledgeDataRepository.findByKnowledgeDataIdAndDeletedAtIsNull(300L)).thenReturn(Optional.of(knowledgeData));

		service.delete(1L, 300L);

		assertThat(knowledgeData.getDeletedAt()).isNotNull();
		assertThat(knowledgeData.getIsDeleted()).isEqualTo("Y");
	}

	@Test
	void delete_rejectsTeamAdminForOtherDepartmentKnowledgeData() {
		TeamAdminKnowledgeDataService service = service();
		User actor = user(1L, UserRole.TEAM_ADMIN, 10L);
		KnowledgeData knowledgeData = knowledgeData(300L, 100L, 20L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
		when(knowledgeDataRepository.findByKnowledgeDataIdAndDeletedAtIsNull(300L)).thenReturn(Optional.of(knowledgeData));

		assertThatThrownBy(() -> service.delete(1L, 300L))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.KNOWLEDGE_DATA_FORBIDDEN);
	}

	private TeamAdminKnowledgeDataService service() {
		return new TeamAdminKnowledgeDataService(
			knowledgeDataRepository,
			ticketRepository,
			ticketAnswerRepository,
			userRepository,
			pointService,
			aiSyncJobService
		);
	}

	private Ticket completedTicket(Long ticketId, Long departmentId) {
		Ticket ticket = assignedTicket(ticketId, departmentId);
		ticket.complete();
		return ticket;
	}

	private Ticket assignedTicket(Long ticketId, Long departmentId) {
		Ticket ticket = Ticket.create(1L, null, TicketPriority.MEDIUM, "title", "content");
		ticket.applyRouting(departmentId, null, BigDecimal.valueOf(95, 2), RoutingDecision.AUTO_ASSIGNED);
		ReflectionTestUtils.setField(ticket, "ticketId", ticketId);
		return ticket;
	}

	private TicketAnswer answer(Long answerId, Long ticketId, String content) {
		TicketAnswer answer = TicketAnswer.create(ticketId, 1L, content, null, null, null, null, null);
		ReflectionTestUtils.setField(answer, "ticketAnswerId", answerId);
		return answer;
	}

	private KnowledgeData knowledgeData(Long knowledgeDataId, Long ticketId, Long departmentId) {
		KnowledgeData knowledgeData = KnowledgeData.approve(ticketId, "question", "answer", departmentId, 1L);
		ReflectionTestUtils.setField(knowledgeData, "knowledgeDataId", knowledgeDataId);
		return knowledgeData;
	}

	private User user(Long userId, UserRole role, Long departmentId) {
		User user = mock(User.class);
		Department department = mock(Department.class);
		lenient().when(user.getUserId()).thenReturn(userId);
		lenient().when(user.getRole()).thenReturn(role);
		lenient().when(user.getDepartment()).thenReturn(department);
		lenient().when(department.getDepartmentId()).thenReturn(departmentId);
		return user;
	}
}
