package com.wip.workipedia.esg.dto;

public record EsgMetricsResponse(
	long knowledgeShareCount,
	long acceptedAnswerCount,
	long estimatedSavedMinutes,
	double sourceBackedAnswerRate,
	double ticketCompletionRate,
	double knowledgeConversionRate,
	double autoAssignmentSuccessRate
) {
}
