package com.wip.workipedia.admin.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public record MonthlyAutoAssignmentRateResponse(
	int months,
	List<MonthlyAutoAssignmentRatePoint> points
) {
	public record MonthlyAutoAssignmentRatePoint(
		String month,
		long totalTicketCount,
		long autoAssignedTicketCount,
		BigDecimal autoAssignmentRate
	) {
	}
}
