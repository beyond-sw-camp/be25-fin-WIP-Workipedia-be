package com.wip.workipedia.knowledge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.knowledge.repository.KnowledgeDataRepository;
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
class KnowledgeBoardServiceTest {

	@Mock
	private KnowledgeDataRepository knowledgeDataRepository;

	@Test
	void findAll_returnsOnlyVisibleKnowledgeData() {
		KnowledgeBoardService service = new KnowledgeBoardService(knowledgeDataRepository);
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
		KnowledgeBoardService service = new KnowledgeBoardService(knowledgeDataRepository);
		when(knowledgeDataRepository.findBoardById(1L))
			.thenReturn(Optional.of(knowledgeBoard(1L)));

		var response = service.findById(1L);

		assertThat(response.knowledgeDataId()).isEqualTo(1L);
		assertThat(response.answer()).isEqualTo("answer");
	}

	@Test
	void findById_rejectsMissingOrDeletedKnowledgeData() {
		KnowledgeBoardService service = new KnowledgeBoardService(knowledgeDataRepository);
		when(knowledgeDataRepository.findBoardById(1L))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.findById(1L))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.KNOWLEDGE_DATA_NOT_FOUND);
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
