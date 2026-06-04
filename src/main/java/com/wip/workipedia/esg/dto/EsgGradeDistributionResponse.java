package com.wip.workipedia.esg.dto;

public record EsgGradeDistributionResponse(
	Integer gradeId,
	String gradeName,
	long userCount
) {
}
