package com.wip.workipedia.admin.dashboard.dto;

import java.util.List;

public record MonthlyTicketTrendResponse(
	int months,
	List<MonthlyTicketTrendPoint> points
) {
	public record MonthlyTicketTrendPoint(
		String month,
		long ticketCount
	) {
	}
}
