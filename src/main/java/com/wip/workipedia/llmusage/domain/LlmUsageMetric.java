package com.wip.workipedia.llmusage.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "llm_usage_metrics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LlmUsageMetric {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "llm_usage_metric_id")
	private Long llmUsageMetricId;

	@Column(name = "chatbot_message_id", nullable = false)
	private Long chatbotMessageId;

	@Column(length = 50)
	private String provider;

	@Column(length = 100)
	private String model;

	@Column(name = "question_snapshot", length = 500)
	private String questionSnapshot;

	@Column(nullable = false, columnDefinition = "CHAR(1)")
	private String answerable;

	@Column(name = "prompt_tokens")
	private Long promptTokens;

	@Column(name = "completion_tokens")
	private Long completionTokens;

	@Column(name = "total_tokens")
	private Long totalTokens;

	@Column(name = "source_count", nullable = false)
	private int sourceCount;

	@Column(name = "cited_chunk_count", nullable = false)
	private int citedChunkCount;

	@Column(name = "full_file_tokens", nullable = false)
	private long fullFileTokens;

	@Column(name = "rag_tokens", nullable = false)
	private long ragTokens;

	@Column(name = "saved_tokens", nullable = false)
	private long savedTokens;

	@Column(name = "reduction_rate", nullable = false, precision = 5, scale = 2)
	private BigDecimal reductionRate;

	@Column(name = "full_file_credits", nullable = false)
	private long fullFileCredits;

	@Column(name = "rag_credits", nullable = false)
	private long ragCredits;

	@Column(name = "saved_credits", nullable = false)
	private long savedCredits;

	@Column(name = "full_file_calls", nullable = false)
	private int fullFileCalls;

	@Column(name = "rag_calls", nullable = false)
	private int ragCalls;

	@Column(name = "saved_calls", nullable = false)
	private int savedCalls;

	@Column(name = "source_breakdown", columnDefinition = "JSON")
	private String sourceBreakdown;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Column(name = "is_deleted", nullable = false, columnDefinition = "CHAR(1)")
	private String isDeleted = "N";

	public static LlmUsageMetric create(CreateArgs args) {
		LlmUsageMetric metric = new LlmUsageMetric();
		metric.chatbotMessageId = args.chatbotMessageId();
		metric.provider = args.provider();
		metric.model = args.model();
		metric.questionSnapshot = args.questionSnapshot();
		metric.answerable = args.answerable() ? "Y" : "N";
		metric.promptTokens = args.promptTokens();
		metric.completionTokens = args.completionTokens();
		metric.totalTokens = args.totalTokens();
		metric.sourceCount = args.sourceCount();
		metric.citedChunkCount = args.citedChunkCount();
		metric.fullFileTokens = args.fullFileTokens();
		metric.ragTokens = args.ragTokens();
		metric.savedTokens = args.savedTokens();
		metric.reductionRate = args.reductionRate();
		metric.fullFileCredits = args.fullFileCredits();
		metric.ragCredits = args.ragCredits();
		metric.savedCredits = args.savedCredits();
		metric.fullFileCalls = args.fullFileCalls();
		metric.ragCalls = args.ragCalls();
		metric.savedCalls = args.savedCalls();
		metric.sourceBreakdown = args.sourceBreakdown();
		metric.createdAt = LocalDateTime.now();
		metric.updatedAt = metric.createdAt;
		return metric;
	}

	public record CreateArgs(
		Long chatbotMessageId,
		String provider,
		String model,
		String questionSnapshot,
		boolean answerable,
		Long promptTokens,
		Long completionTokens,
		Long totalTokens,
		int sourceCount,
		int citedChunkCount,
		long fullFileTokens,
		long ragTokens,
		long savedTokens,
		BigDecimal reductionRate,
		long fullFileCredits,
		long ragCredits,
		long savedCredits,
		int fullFileCalls,
		int ragCalls,
		int savedCalls,
		String sourceBreakdown
	) {
	}
}
