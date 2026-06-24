package com.wip.workipedia.admin.team.knowledge.dto;

import com.wip.workipedia.knowledge.domain.KnowledgeData;
import com.wip.workipedia.ticket.dto.TicketFileResponse;
import java.time.LocalDateTime;
import java.util.List;

public record KnowledgeDataResponse(
	Long knowledgeDataId,
	Long ticketId,
	Long departmentId,
	String question,
	String answer,
	Long approvedBy,
	LocalDateTime approvedAt,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	String fileUrl,
	List<TicketFileResponse> files
) {
	public static KnowledgeDataResponse from(KnowledgeData knowledgeData) {
		return from(knowledgeData, List.of());
	}

	public static KnowledgeDataResponse from(KnowledgeData knowledgeData, List<TicketFileResponse> files) {
		return new KnowledgeDataResponse(
			knowledgeData.getKnowledgeDataId(),
			knowledgeData.getTicketId(),
			knowledgeData.getDepartmentId(),
			knowledgeData.getTitle(),
			knowledgeData.getContent(),
			knowledgeData.getApprovedBy(),
			knowledgeData.getApprovedAt(),
			knowledgeData.getCreatedAt(),
			knowledgeData.getUpdatedAt(),
			firstFileUrl(files),
			files
		);
	}

	private static String firstFileUrl(List<TicketFileResponse> files) {
		return files == null || files.isEmpty() ? null : files.get(0).fileUrl();
	}
}
