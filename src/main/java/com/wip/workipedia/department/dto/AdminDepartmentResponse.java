package com.wip.workipedia.department.dto;

import com.wip.workipedia.department.domain.Department;

public record AdminDepartmentResponse(
	Long departmentId,
	String departmentName,
	String routingPrompt
) {
	public static AdminDepartmentResponse from(Department department, String routingPrompt) {
		return new AdminDepartmentResponse(
			department.getDepartmentId(),
			department.getDepartmentName(),
			routingPrompt
		);
	}
}
