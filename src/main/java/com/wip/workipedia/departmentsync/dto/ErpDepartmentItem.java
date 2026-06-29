package com.wip.workipedia.departmentsync.dto;

import jakarta.validation.constraints.NotBlank;

public record ErpDepartmentItem(
	@NotBlank String externalId,
	@NotBlank String departmentName,
	String dutyDesc,
	String useYn
) {}
