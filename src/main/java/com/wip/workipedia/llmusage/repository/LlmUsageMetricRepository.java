package com.wip.workipedia.llmusage.repository;

import com.wip.workipedia.llmusage.domain.LlmUsageMetric;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LlmUsageMetricRepository extends JpaRepository<LlmUsageMetric, Long> {

	boolean existsByChatbotMessageId(Long chatbotMessageId);

	List<LlmUsageMetric> findByIsDeletedOrderByCreatedAtDesc(String isDeleted, Pageable pageable);

	@Query("""
		select m.sourceBreakdown
		from LlmUsageMetric m
		where m.isDeleted = 'N'
		  and m.sourceBreakdown is not null
		""")
	List<String> findActiveSourceBreakdowns();

	@Query("""
		select
			coalesce(sum(m.fullFileTokens), 0) as fullFileTokens,
			coalesce(sum(m.ragTokens), 0) as ragTokens,
			coalesce(sum(m.savedTokens), 0) as savedTokens,
			coalesce(sum(m.fullFileCredits), 0) as fullFileCredits,
			coalesce(sum(m.ragCredits), 0) as ragCredits,
			coalesce(sum(m.savedCredits), 0) as savedCredits,
			coalesce(sum(m.fullFileCalls), 0) as fullFileCalls,
			coalesce(sum(m.ragCalls), 0) as ragCalls,
			coalesce(sum(m.savedCalls), 0) as savedCalls,
			coalesce(sum(m.sourceCount), 0) as sourceCount,
			coalesce(sum(m.citedChunkCount), 0) as citedChunkCount,
			count(m) as sampleCount
		from LlmUsageMetric m
		where m.isDeleted = 'N'
		""")
	LlmUsageSummaryProjection summarizeActive();

	interface LlmUsageSummaryProjection {
		Long getFullFileTokens();
		Long getRagTokens();
		Long getSavedTokens();
		Long getFullFileCredits();
		Long getRagCredits();
		Long getSavedCredits();
		Long getFullFileCalls();
		Long getRagCalls();
		Long getSavedCalls();
		Long getSourceCount();
		Long getCitedChunkCount();
		Long getSampleCount();
	}

}
