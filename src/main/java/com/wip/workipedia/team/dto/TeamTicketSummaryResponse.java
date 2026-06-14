package com.wip.workipedia.team.dto;

public record TeamTicketSummaryResponse(
	Long departmentId,
	String departmentName,
	long totalCount,
	long myAnsweredCount,
	long assignedCount,
	long completedCount
) {
}
