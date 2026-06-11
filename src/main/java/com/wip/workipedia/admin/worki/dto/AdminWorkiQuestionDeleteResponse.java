package com.wip.workipedia.admin.worki.dto;

public record AdminWorkiQuestionDeleteResponse(
	Long questionId,
	Long authorId,
	int deductedPoint,
	long remainingPoint
) {
}
