package com.wip.workipedia.team.dto;

public record TeamTicketSummaryResponse(
	Long departmentId,
	String departmentName,
	long yearlyAssignedCount,
	long myVisibleAnsweredCount,
	long assignedCount,
	long completedCount
) {
}
