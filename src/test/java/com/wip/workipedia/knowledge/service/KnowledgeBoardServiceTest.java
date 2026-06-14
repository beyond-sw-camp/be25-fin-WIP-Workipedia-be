package com.wip.workipedia.knowledge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.knowledge.domain.KnowledgeData;
import com.wip.workipedia.knowledge.repository.KnowledgeDataRepository;
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
		when(knowledgeDataRepository.findByDeletedAtIsNullAndIsDeleted("N", pageable))
			.thenReturn(new PageImpl<>(List.of(knowledgeData(1L)), pageable, 1));

		var response = service.findAll(pageable);

		assertThat(response.content()).hasSize(1);
		assertThat(response.content().getFirst().knowledgeDataId()).isEqualTo(1L);
		assertThat(response.content().getFirst().question()).isEqualTo("question");
	}

	@Test
	void findById_returnsVisibleKnowledgeData() {
		KnowledgeBoardService service = new KnowledgeBoardService(knowledgeDataRepository);
		when(knowledgeDataRepository.findByKnowledgeDataIdAndDeletedAtIsNullAndIsDeleted(1L, "N"))
			.thenReturn(Optional.of(knowledgeData(1L)));

		var response = service.findById(1L);

		assertThat(response.knowledgeDataId()).isEqualTo(1L);
		assertThat(response.answer()).isEqualTo("answer");
	}

	@Test
	void findById_rejectsMissingOrDeletedKnowledgeData() {
		KnowledgeBoardService service = new KnowledgeBoardService(knowledgeDataRepository);
		when(knowledgeDataRepository.findByKnowledgeDataIdAndDeletedAtIsNullAndIsDeleted(1L, "N"))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.findById(1L))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.KNOWLEDGE_DATA_NOT_FOUND);
	}

	private KnowledgeData knowledgeData(Long knowledgeDataId) {
		KnowledgeData knowledgeData = KnowledgeData.approve(100L, "question", "answer", 10L, 1L);
		ReflectionTestUtils.setField(knowledgeData, "knowledgeDataId", knowledgeDataId);
		return knowledgeData;
	}
}
