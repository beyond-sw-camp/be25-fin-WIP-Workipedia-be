package com.wip.workipedia.admin.team.knowledge.dto;

import com.wip.workipedia.knowledge.domain.KnowledgeData;
import java.time.LocalDateTime;

public record KnowledgeDataResponse(
	Long knowledgeDataId,
	Long ticketId,
	Long departmentId,
	String question,
	String answer,
	Long approvedBy,
	LocalDateTime approvedAt,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static KnowledgeDataResponse from(KnowledgeData knowledgeData) {
		return new KnowledgeDataResponse(
			knowledgeData.getKnowledgeDataId(),
			knowledgeData.getTicketId(),
			knowledgeData.getDepartmentId(),
			knowledgeData.getTitle(),
			knowledgeData.getContent(),
			knowledgeData.getApprovedBy(),
			knowledgeData.getApprovedAt(),
			knowledgeData.getCreatedAt(),
			knowledgeData.getUpdatedAt()
		);
	}
}
