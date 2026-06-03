package com.wip.workipedia.department.dto;

import com.wip.workipedia.department.domain.Department;

public record DepartmentResponse(
	Long departmentId,
	String departmentName
) {
	public static DepartmentResponse from(Department department) {
		return new DepartmentResponse(
			department.getDepartmentId(),
			department.getName()
		);
	}
}
