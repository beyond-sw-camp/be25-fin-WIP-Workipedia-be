package com.wip.workipedia.admin.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.domain.UserRole;
import com.wip.workipedia.user.domain.UserStatus;
import com.wip.workipedia.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Test
	void changeRole_promotesActiveUserToTeamAdmin() {
		AdminUserService service = service();
		User user = user(10L, UserRole.USER, UserStatus.ACTIVE, null);
		when(userRepository.findById(10L)).thenReturn(Optional.of(user));

		var response = service.changeRole(10L, UserRole.TEAM_ADMIN);

		assertThat(user.getRole()).isEqualTo(UserRole.TEAM_ADMIN);
		assertThat(response.userId()).isEqualTo(10L);
		assertThat(response.role()).isEqualTo(UserRole.TEAM_ADMIN.name());
	}

	@Test
	void changeRole_rejectsUnsupportedRole() {
		AdminUserService service = service();

		assertThatThrownBy(() -> service.changeRole(10L, UserRole.SYSTEM_ADMIN))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.BAD_REQUEST);
	}

	@Test
	void changeRole_rejectsMissingUser() {
		AdminUserService service = service();
		when(userRepository.findById(10L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.changeRole(10L, UserRole.TEAM_ADMIN))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.NOT_FOUND);
	}

	@Test
	void changeRole_rejectsAlreadyTeamAdmin() {
		AdminUserService service = service();
		User user = user(10L, UserRole.TEAM_ADMIN, UserStatus.ACTIVE, null);
		when(userRepository.findById(10L)).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> service.changeRole(10L, UserRole.TEAM_ADMIN))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.CONFLICT);
	}

	@Test
	void changeRole_rejectsInactiveUser() {
		AdminUserService service = service();
		User user = user(10L, UserRole.USER, UserStatus.INACTIVE, null);
		when(userRepository.findById(10L)).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> service.changeRole(10L, UserRole.TEAM_ADMIN))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.FORBIDDEN);
	}

	@Test
	void changeRole_rejectsDeletedUser() {
		AdminUserService service = service();
		User user = user(10L, UserRole.USER, UserStatus.ACTIVE, LocalDateTime.now());
		when(userRepository.findById(10L)).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> service.changeRole(10L, UserRole.TEAM_ADMIN))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.FORBIDDEN);
	}

	private AdminUserService service() {
		return new AdminUserService(userRepository);
	}

	private User user(Long userId, UserRole role, UserStatus status, LocalDateTime deletedAt) {
		Department department = mock(Department.class);
		lenient().when(department.getDepartmentId()).thenReturn(1L);
		lenient().when(department.getDepartmentName()).thenReturn("개발팀");

		User user = User.signup(department, "EMP-" + userId, "user" + userId + "@example.com", "password", "사용자");
		ReflectionTestUtils.setField(user, "userId", userId);
		ReflectionTestUtils.setField(user, "role", role);
		ReflectionTestUtils.setField(user, "status", status);
		ReflectionTestUtils.setField(user, "deletedAt", deletedAt);
		return user;
	}
}
