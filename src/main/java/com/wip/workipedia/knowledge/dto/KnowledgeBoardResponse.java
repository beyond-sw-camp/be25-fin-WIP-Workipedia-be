package com.wip.workipedia.knowledge.dto;

import com.wip.workipedia.knowledge.repository.KnowledgeDataRepository;
import java.time.LocalDateTime;

public record KnowledgeBoardResponse(
	Long knowledgeDataId,
	Long departmentId,
	String departmentName,
	String question,
	String answer,
	LocalDateTime approvedAt,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static KnowledgeBoardResponse from(KnowledgeDataRepository.KnowledgeBoardProjection projection) {
		return new KnowledgeBoardResponse(
			projection.getKnowledgeDataId(),
			projection.getDepartmentId(),
			projection.getDepartmentName(),
			projection.getQuestion(),
			projection.getAnswer(),
			projection.getApprovedAt(),
			projection.getCreatedAt(),
			projection.getUpdatedAt()
		);
	}
}
