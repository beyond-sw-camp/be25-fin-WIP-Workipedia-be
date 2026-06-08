package com.wip.workipedia.mypage.dto;

import java.util.List;

public record MyPageResponse(
	UserSummary user,
	TicketSummary ticket,
	PointSummary point,
	NotificationSettings notificationSettings,
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

	public record NotificationSettings(
		boolean allEnabled,
		boolean ticketEnabled,
		boolean boardEnabled,
		boolean manualEnabled
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
