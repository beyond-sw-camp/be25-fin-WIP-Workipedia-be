package com.wip.workipedia.esg.dto;

public record EsgLeaderboardResponse(
	int rank,
	long userId,
	String nickname,
	String departmentName,
	long esgScore,
	String gradeName,
	String gradeImageUrl,
	long answerCount,
	long acceptedAnswerCount
) {
	public static EsgLeaderboardResponse of(
		int rank,
		long userId,
		String nickname,
		String departmentName,
		long esgScore,
		String gradeName,
		String gradeImageUrl,
		long answerCount,
		long acceptedAnswerCount
	) {
		return new EsgLeaderboardResponse(
			rank,
			userId,
			nickname,
			departmentName,
			esgScore,
			gradeName,
			gradeImageUrl,
			answerCount,
			acceptedAnswerCount
		);
	}
}
