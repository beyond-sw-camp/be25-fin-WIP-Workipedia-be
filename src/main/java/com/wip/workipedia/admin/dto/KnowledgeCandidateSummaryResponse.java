package com.wip.workipedia.admin.dto;

import java.time.LocalDateTime;

public record KnowledgeCandidateSummaryResponse(
	long candidateId,
	long ticketId,
	String draftTitle,
	String status,
	LocalDateTime createdAt
) {
}
