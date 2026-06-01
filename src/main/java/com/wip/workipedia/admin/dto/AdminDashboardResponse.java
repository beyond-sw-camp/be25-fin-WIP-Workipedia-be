package com.wip.workipedia.admin.dto;

public record AdminDashboardResponse(
	long teamQueueCount,
	long commonQueueCount,
	long completedTicketCount,
	long knowledgeCandidateCount,
	long knowledgePublishedCount,
	double autoAssignmentSuccessRate,
	long estimatedSavedMinutes
) {
}
