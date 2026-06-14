package com.wip.workipedia.admin.team.dashboard.dto;

import java.util.List;

public record MonthlyTrendResponse(
	Long departmentId,
	String departmentName,
	int months,
	List<MonthlyPoint> points
) {
	public record MonthlyPoint(
		String month,
		long count
	) {
	}
}
