package com.wip.workipedia.knowledge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wip.workipedia.aisync.domain.AiSyncOperation;
import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.aisync.service.AiSyncJobService;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.knowledge.domain.KnowledgeData;
import com.wip.workipedia.knowledge.repository.KnowledgeDataRepository;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.domain.UserRole;
import com.wip.workipedia.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class KnowledgeBoardServiceTest {

	@Mock
	private KnowledgeDataRepository knowledgeDataRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private AiSyncJobService aiSyncJobService;

	@Test
	void findAll_returnsOnlyVisibleKnowledgeData() {
		KnowledgeBoardService service = service();
		var pageable = PageRequest.of(0, 10);
		when(knowledgeDataRepository.findBoard(pageable))
			.thenReturn(new PageImpl<>(List.of(knowledgeBoard(1L)), pageable, 1));

		var response = service.findAll(pageable);

		assertThat(response.content()).hasSize(1);
		assertThat(response.content().getFirst().knowledgeDataId()).isEqualTo(1L);
		assertThat(response.content().getFirst().question()).isEqualTo("question");
	}

	@Test
	void findById_returnsVisibleKnowledgeData() {
		KnowledgeBoardService service = service();
		when(knowledgeDataRepository.findBoardById(1L))
			.thenReturn(Optional.of(knowledgeBoard(1L)));

		var response = service.findById(1L);

		assertThat(response.knowledgeDataId()).isEqualTo(1L);
		assertThat(response.answer()).isEqualTo("answer");
	}

	@Test
	void findById_rejectsMissingOrDeletedKnowledgeData() {
		KnowledgeBoardService service = service();
		when(knowledgeDataRepository.findBoardById(1L))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.findById(1L))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.KNOWLEDGE_DATA_NOT_FOUND);
	}

	@Test
	void delete_allowsSystemAdminForAnyDepartmentKnowledgeData() {
		KnowledgeBoardService service = service();
		User actor = user(1L, UserRole.SYSTEM_ADMIN, null);
		KnowledgeData knowledgeData = knowledgeData(100L, 10L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
		when(knowledgeDataRepository.findByKnowledgeDataIdAndDeletedAtIsNull(100L))
			.thenReturn(Optional.of(knowledgeData));

		service.delete(1L, 100L);

		assertThat(knowledgeData.getDeletedAt()).isNotNull();
		assertThat(knowledgeData.getIsDeleted()).isEqualTo("Y");
		verify(aiSyncJobService).enqueue(
			eq(AiSyncSourceType.KNOWLEDGE_DATA),
			eq(100L),
			eq(AiSyncOperation.DELETE)
		);
	}

	@Test
	void delete_allowsTeamAdminForOwnDepartmentKnowledgeData() {
		KnowledgeBoardService service = service();
		User actor = user(1L, UserRole.TEAM_ADMIN, 10L);
		KnowledgeData knowledgeData = knowledgeData(100L, 10L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
		when(knowledgeDataRepository.findByKnowledgeDataIdAndDeletedAtIsNull(100L))
			.thenReturn(Optional.of(knowledgeData));

		service.delete(1L, 100L);

		assertThat(knowledgeData.getDeletedAt()).isNotNull();
		assertThat(knowledgeData.getIsDeleted()).isEqualTo("Y");
	}

	@Test
	void delete_rejectsTeamAdminForOtherDepartmentKnowledgeData() {
		KnowledgeBoardService service = service();
		User actor = user(1L, UserRole.TEAM_ADMIN, 10L);
		KnowledgeData knowledgeData = knowledgeData(100L, 20L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
		when(knowledgeDataRepository.findByKnowledgeDataIdAndDeletedAtIsNull(100L))
			.thenReturn(Optional.of(knowledgeData));

		assertThatThrownBy(() -> service.delete(1L, 100L))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.KNOWLEDGE_DATA_FORBIDDEN);
	}

	@Test
	void delete_rejectsRegularUser() {
		KnowledgeBoardService service = service();
		User actor = user(1L, UserRole.USER, 10L);
		KnowledgeData knowledgeData = knowledgeData(100L, 10L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
		when(knowledgeDataRepository.findByKnowledgeDataIdAndDeletedAtIsNull(100L))
			.thenReturn(Optional.of(knowledgeData));

		assertThatThrownBy(() -> service.delete(1L, 100L))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.KNOWLEDGE_DATA_FORBIDDEN);
	}

	private KnowledgeBoardService service() {
		return new KnowledgeBoardService(knowledgeDataRepository, userRepository, aiSyncJobService);
	}

	private KnowledgeData knowledgeData(Long knowledgeDataId, Long departmentId) {
		KnowledgeData knowledgeData = KnowledgeData.approve(1L, "question", "answer", departmentId, 1L);
		org.springframework.test.util.ReflectionTestUtils.setField(knowledgeData, "knowledgeDataId", knowledgeDataId);
		return knowledgeData;
	}

	private User user(Long userId, UserRole role, Long departmentId) {
		User user = mock(User.class);
		lenient().when(user.getUserId()).thenReturn(userId);
		lenient().when(user.getRole()).thenReturn(role);
		if (departmentId != null) {
			Department department = mock(Department.class);
			lenient().when(user.getDepartment()).thenReturn(department);
			lenient().when(department.getDepartmentId()).thenReturn(departmentId);
		}
		return user;
	}

	private KnowledgeDataRepository.KnowledgeBoardProjection knowledgeBoard(Long knowledgeDataId) {
		return new KnowledgeDataRepository.KnowledgeBoardProjection() {
			@Override
			public Long getKnowledgeDataId() {
				return knowledgeDataId;
			}

			@Override
			public Long getDepartmentId() {
				return 10L;
			}

			@Override
			public String getDepartmentName() {
				return "department";
			}

			@Override
			public String getQuestion() {
				return "question";
			}

			@Override
			public String getAnswer() {
				return "answer";
			}

			@Override
			public LocalDateTime getApprovedAt() {
				return LocalDateTime.now();
			}

			@Override
			public LocalDateTime getCreatedAt() {
				return LocalDateTime.now();
			}

			@Override
			public LocalDateTime getUpdatedAt() {
				return LocalDateTime.now();
			}
		};
	}
}
