package com.wip.workipedia.admin.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public record DepartmentAutoAssignmentRateResponse(
	List<DepartmentAutoAssignmentRateItem> departments
) {
	public record DepartmentAutoAssignmentRateItem(
		Long departmentId,
		String departmentName,
		long totalTicketCount,
		long autoAssignedTicketCount,
		BigDecimal autoAssignmentRate
	) {
	}
}
