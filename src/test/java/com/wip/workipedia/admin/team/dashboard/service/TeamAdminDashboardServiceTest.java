package com.wip.workipedia.admin.team.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.knowledge.repository.KnowledgeDataRepository;
import com.wip.workipedia.ticket.repository.TicketRepository;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.domain.UserRole;
import com.wip.workipedia.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeamAdminDashboardServiceTest {

	@Mock
	private KnowledgeDataRepository knowledgeDataRepository;

	@Mock
	private TicketRepository ticketRepository;

	@Mock
	private UserRepository userRepository;

	@Test
	void getKnowledgeTrend_fillsMissingMonthsWithZero() {
		TeamAdminDashboardService service = service();
		User actor = user(UserRole.TEAM_ADMIN, 10L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
		when(knowledgeDataRepository.countMonthlyApprovedByDepartment(any(), any(), any()))
			.thenReturn(List.of(monthlyKnowledgeCount("2099-01", 3L)));

		var response = service.getKnowledgeTrend(1L, 3);

		assertThat(response.departmentId()).isEqualTo(10L);
		assertThat(response.months()).isEqualTo(3);
		assertThat(response.points()).hasSize(3);
		assertThat(response.points()).allSatisfy(point -> assertThat(point.count()).isGreaterThanOrEqualTo(0L));
	}

	@Test
	void getChatbotAssignmentTrend_rejectsNonTeamAdmin() {
		TeamAdminDashboardService service = service();
		User actor = user(UserRole.USER, 10L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));

		assertThatThrownBy(() -> service.getChatbotAssignmentTrend(1L, 6))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.KNOWLEDGE_DATA_FORBIDDEN);
	}

	@Test
	void getKnowledgeTrend_rejectsInvalidMonths() {
		TeamAdminDashboardService service = service();
		User actor = user(UserRole.TEAM_ADMIN, 10L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));

		assertThatThrownBy(() -> service.getKnowledgeTrend(1L, 13))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.BAD_REQUEST);
	}

	private TeamAdminDashboardService service() {
		return new TeamAdminDashboardService(knowledgeDataRepository, ticketRepository, userRepository);
	}

	private User user(UserRole role, Long departmentId) {
		User user = mock(User.class);
		Department department = mock(Department.class);
		lenient().when(user.getRole()).thenReturn(role);
		lenient().when(user.getDepartment()).thenReturn(department);
		lenient().when(department.getDepartmentId()).thenReturn(departmentId);
		lenient().when(department.getDepartmentName()).thenReturn("department-" + departmentId);
		return user;
	}

	private KnowledgeDataRepository.MonthlyCountProjection monthlyKnowledgeCount(String month, long count) {
		return new KnowledgeDataRepository.MonthlyCountProjection() {
			@Override
			public String getMonth() {
				return month;
			}

			@Override
			public long getCount() {
				return count;
			}
		};
	}
}
