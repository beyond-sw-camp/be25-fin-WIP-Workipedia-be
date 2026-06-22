package com.wip.workipedia.admin.department.dto;

import com.wip.workipedia.department.domain.Department;

public record AdminDepartmentResponse(
	Long departmentId,
	String departmentName,
	String routingPrompt,
	long memberCount,
	AdminDepartmentSyncStatus syncStatus,
	String syncInfo
) {
	public static AdminDepartmentResponse from(Department department, String routingPrompt) {
		return from(department, routingPrompt, 0);
	}

	public static AdminDepartmentResponse from(Department department, String routingPrompt, long memberCount) {
		return from(department, routingPrompt, memberCount, AdminDepartmentSyncStatus.EMPTY, null);
	}

	public static AdminDepartmentResponse from(
		Department department,
		String routingPrompt,
		long memberCount,
		AdminDepartmentSyncStatus syncStatus,
		String syncInfo
	) {
		return new AdminDepartmentResponse(
			department.getDepartmentId(),
			department.getDepartmentName(),
			routingPrompt,
			memberCount,
			syncStatus,
			syncInfo
		);
	}
}
