package com.wip.workipedia.knowledge.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.knowledge.dto.KnowledgeBoardResponse;
import com.wip.workipedia.knowledge.repository.KnowledgeDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeBoardService {

	private final KnowledgeDataRepository knowledgeDataRepository;

	public PageResponse<KnowledgeBoardResponse> findAll(Pageable pageable) {
		return PageResponse.from(
			knowledgeDataRepository.findBoard(pageable)
				.map(KnowledgeBoardResponse::from)
		);
	}

	public KnowledgeBoardResponse findById(Long knowledgeDataId) {
		return knowledgeDataRepository.findBoardById(knowledgeDataId)
			.map(KnowledgeBoardResponse::from)
			.orElseThrow(() -> new CustomException(ErrorType.KNOWLEDGE_DATA_NOT_FOUND));
	}
}
