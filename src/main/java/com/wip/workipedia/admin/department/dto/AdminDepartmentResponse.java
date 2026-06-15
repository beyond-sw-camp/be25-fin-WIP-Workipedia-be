package com.wip.workipedia.admin.department.dto;

import com.wip.workipedia.department.domain.Department;

public record AdminDepartmentResponse(
	Long departmentId,
	String departmentName,
	String routingPrompt,
	long memberCount
) {
	public static AdminDepartmentResponse from(Department department, String routingPrompt) {
		return from(department, routingPrompt, 0);
	}

	public static AdminDepartmentResponse from(Department department, String routingPrompt, long memberCount) {
		return new AdminDepartmentResponse(
			department.getDepartmentId(),
			department.getDepartmentName(),
			routingPrompt,
			memberCount
		);
	}
}
