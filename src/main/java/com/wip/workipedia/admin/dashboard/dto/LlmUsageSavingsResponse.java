package com.wip.workipedia.admin.dashboard.dto;

import java.time.LocalDateTime;
import java.util.List;

public record LlmUsageSavingsResponse(
	Summary summary,
	List<RecentSample> recentSamples,
	List<SourceBreakdown> sourceBreakdown
) {
	public record Summary(
		long sampleCount,
		long fullFileTokens,
		long ragTokens,
		long savedTokens,
		long reductionRate,
		long fullFileCredits,
		long ragCredits,
		long savedCredits,
		long fullFileCalls,
		long ragCalls,
		long savedCalls,
		long sourceCount,
		long citedChunkCount,
		long averageFullFileTokens,
		long averageRagTokens
	) {
	}

	public record RecentSample(
		Long id,
		Long chatbotMessageId,
		LocalDateTime createdAt,
		String question,
		boolean answerable,
		int sourceCount,
		int citedChunkCount,
		long fullFileTokens,
		long ragTokens,
		long savedTokens,
		long reductionRate,
		long fullFileCredits,
		long ragCredits,
		long savedCredits,
		int fullFileCalls,
		int ragCalls,
		int savedCalls,
		List<SourceBreakdown> sourceBreakdown
	) {
	}

	public record SourceBreakdown(
		String type,
		int count
	) {
	}
}
