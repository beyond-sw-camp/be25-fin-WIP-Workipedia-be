package com.wip.workipedia.knowledge.dto;

import com.wip.workipedia.knowledge.repository.KnowledgeDataRepository;
import com.wip.workipedia.ticket.dto.TicketFileResponse;
import java.time.LocalDateTime;
import java.util.List;

public record KnowledgeBoardResponse(
	Long knowledgeDataId,
	Long ticketId,
	Long departmentId,
	String departmentName,
	String question,
	String answer,
	LocalDateTime approvedAt,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	String fileUrl,
	List<TicketFileResponse> files
) {
	public static KnowledgeBoardResponse from(KnowledgeDataRepository.KnowledgeBoardProjection projection) {
		return from(projection, List.of());
	}

	public static KnowledgeBoardResponse from(
		KnowledgeDataRepository.KnowledgeBoardProjection projection,
		List<TicketFileResponse> files
	) {
		return new KnowledgeBoardResponse(
			projection.getKnowledgeDataId(),
			projection.getTicketId(),
			projection.getDepartmentId(),
			projection.getDepartmentName(),
			projection.getQuestion(),
			projection.getAnswer(),
			projection.getApprovedAt(),
			projection.getCreatedAt(),
			projection.getUpdatedAt(),
			firstFileUrl(files),
			files
		);
	}

	private static String firstFileUrl(List<TicketFileResponse> files) {
		return files == null || files.isEmpty() ? null : files.get(0).fileUrl();
	}
}
