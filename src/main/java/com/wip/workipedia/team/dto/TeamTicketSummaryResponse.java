package com.wip.workipedia.team.dto;

public record TeamTicketSummaryResponse(
	Long departmentId,
	String departmentName,
	long totalCount,
	long assignedCount,
	long completedCount
) {
}
