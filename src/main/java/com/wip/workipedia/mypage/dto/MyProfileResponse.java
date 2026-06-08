package com.wip.workipedia.mypage.dto;

import java.util.List;

public record MyProfileResponse(
	UserSummary user,
	TicketSummary ticket,
	PointSummary point,
	EsgGradeSummary esgGrade,
	List<EsgGradeProgress> esgGradeProgress
) {

	public record UserSummary(
		Long userId,
		String nickname,
		String role,
		String status
	) {
	}

	public record TicketSummary(
		long createdTicketCount
	) {
	}

	public record PointSummary(
		long currentPoint,
		long esgScore
	) {
	}

	public record EsgGradeSummary(
		Integer gradeId,
		String gradeName,
		String gradeImageUrl,
		long esgScore,
		long minScore,
		Long maxScore,
		Long remainingScoreForNextGrade
	) {
	}

	public record EsgGradeProgress(
		Integer gradeId,
		String gradeName,
		long minScore,
		Long maxScore
	) {
	}
}
