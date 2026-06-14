package com.wip.workipedia.knowledge.dto;

import com.wip.workipedia.knowledge.domain.KnowledgeData;
import java.time.LocalDateTime;

public record KnowledgeBoardResponse(
	Long knowledgeDataId,
	Long ticketId,
	Long departmentId,
	String question,
	String answer,
	LocalDateTime approvedAt,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static KnowledgeBoardResponse from(KnowledgeData knowledgeData) {
		return new KnowledgeBoardResponse(
			knowledgeData.getKnowledgeDataId(),
			knowledgeData.getTicketId(),
			knowledgeData.getDepartmentId(),
			knowledgeData.getTitle(),
			knowledgeData.getContent(),
			knowledgeData.getApprovedAt(),
			knowledgeData.getCreatedAt(),
			knowledgeData.getUpdatedAt()
		);
	}
}
