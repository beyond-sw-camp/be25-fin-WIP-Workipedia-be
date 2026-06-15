package com.wip.workipedia.admin.team.dashboard.dto;

public record TeamDashboardSummaryResponse(
	Long departmentId,
	String departmentName,
	long yearlyAssignedCount,
	long myVisibleAnsweredCount,
	long assignedCount,
	long completedCount
) {
}
