package com.wip.workipedia.admin.dashboard.dto;

import java.util.List;

public record DepartmentTicketStatusResponse(
	List<DepartmentTicketStatusItem> departments
) {
	public record DepartmentTicketStatusItem(
		Long departmentId,
		String departmentName,
		long totalTicketCount,
		long assignedTicketCount,
		long completedTicketCount
	) {
	}
}
