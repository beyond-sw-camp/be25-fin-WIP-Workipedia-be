package com.wip.workipedia.admin.dto;

public record EsgGradeDistributionResponse(
	Integer gradeId,
	String gradeName,
	long userCount
) {
}
