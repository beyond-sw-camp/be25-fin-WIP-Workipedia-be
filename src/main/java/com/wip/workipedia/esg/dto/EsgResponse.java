package com.wip.workipedia.esg.dto;

public record EsgResponse(
	long userId,
	long esgScore,
	String gradeName,
	Long nextGradeMinScore,
	Long remainingScoreForNextGrade,
	String gradeImageUrl
) {
}
