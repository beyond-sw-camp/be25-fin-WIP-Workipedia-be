package com.wip.workipedia.llmusage.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.chatbot.ai.SourceItem;
import com.wip.workipedia.llmusage.domain.LlmUsageMetric;
import com.wip.workipedia.llmusage.repository.LlmUsageMetricRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmUsageMetricService {

	private static final double KOREAN_CHARS_PER_TOKEN = 2.4;
	private static final long FULL_FILE_TOKENS_PER_SOURCE = 2_000L;
	private static final long RAG_TOKENS_PER_CHUNK = 450L;
	private static final long PROMPT_OVERHEAD_TOKENS = 180L;
	private static final int FULL_FILE_LLM_CALLS = 2;
	private static final int RAG_LLM_CALLS = 1;
	private static final int QUESTION_SNAPSHOT_LIMIT = 500;

	private final LlmUsageMetricRepository llmUsageMetricRepository;
	private final ObjectMapper objectMapper;

	@Transactional
	public void recordChatbotUsage(
		Long chatbotMessageId,
		String question,
		String answer,
		boolean answerable,
		List<SourceItem> sources
	) {
		if (chatbotMessageId == null || llmUsageMetricRepository.existsByChatbotMessageId(chatbotMessageId)) {
			return;
		}

		List<SourceItem> safeSources = sources == null ? List.of() : sources;
		Set<String> uniqueSources = safeSources.stream()
			.map(this::sourceKey)
			.collect(Collectors.toSet());
		Set<String> uniqueChunks = safeSources.stream()
			.map(this::chunkKey)
			.collect(Collectors.toSet());

		int sourceCount = Math.max(uniqueSources.size(), safeSources.isEmpty() ? 0 : 1);
		int citedChunkCount = Math.max(uniqueChunks.size(), safeSources.isEmpty() ? 0 : safeSources.size());
		long sharedTokens = estimateTextTokens(question) + estimateTextTokens(answer) + PROMPT_OVERHEAD_TOKENS;
		long fullFileTokens = sharedTokens + Math.max(sourceCount, 1) * FULL_FILE_TOKENS_PER_SOURCE;
		long ragTokens = sharedTokens + Math.max(citedChunkCount, answerable ? 1 : 0) * RAG_TOKENS_PER_CHUNK;
		long savedTokens = Math.max(0L, fullFileTokens - ragTokens);

		LlmUsageMetric metric = LlmUsageMetric.create(new LlmUsageMetric.CreateArgs(
			chatbotMessageId,
			null,
			null,
			snapshot(question),
			answerable,
			null,
			null,
			null,
			sourceCount,
			citedChunkCount,
			fullFileTokens,
			ragTokens,
			savedTokens,
			rate(savedTokens, fullFileTokens),
			fullFileTokens,
			ragTokens,
			savedTokens,
			FULL_FILE_LLM_CALLS,
			RAG_LLM_CALLS,
			FULL_FILE_LLM_CALLS - RAG_LLM_CALLS,
			toJson(sourceBreakdown(safeSources))
		));
		llmUsageMetricRepository.save(metric);
	}

	private long estimateTextTokens(String text) {
		if (text == null || text.isBlank()) {
			return 0L;
		}
		return (long) Math.ceil(text.trim().length() / KOREAN_CHARS_PER_TOKEN);
	}

	private String sourceKey(SourceItem source) {
		return String.join(":",
			Objects.toString(source.sourceType(), ""),
			Objects.toString(source.sourceId(), ""),
			Objects.toString(source.fileName(), "")
		);
	}

	private String chunkKey(SourceItem source) {
		if (source.candidateId() != null && !source.candidateId().isBlank()) {
			return source.candidateId();
		}
		return String.join(":",
			Objects.toString(source.sourceType(), ""),
			Objects.toString(source.sourceId(), ""),
			Objects.toString(source.chunkIndex(), "")
		);
	}

	private List<SourceBreakdown> sourceBreakdown(List<SourceItem> sources) {
		Map<String, Integer> counts = new LinkedHashMap<>();
		for (SourceItem source : sources) {
			String type = normalizeSourceType(source.sourceType());
			counts.put(type, counts.getOrDefault(type, 0) + 1);
		}
		return counts.entrySet().stream()
			.map(entry -> new SourceBreakdown(entry.getKey(), entry.getValue()))
			.toList();
	}

	private String normalizeSourceType(String type) {
		return switch (Objects.toString(type, "")) {
			case "MANUAL" -> "규정집";
			case "MANUAL_KNOWLEDGE" -> "수기 지식";
			case "WORKI" -> "워키";
			case "TICKET" -> "티켓";
			case "KNOWLEDGE_DATA" -> "지식 문서";
			case "CHAT" -> "채팅";
			default -> Objects.toString(type, "UNKNOWN");
		};
	}

	private String snapshot(String question) {
		if (question == null) {
			return null;
		}
		String trimmed = question.trim();
		return trimmed.length() <= QUESTION_SNAPSHOT_LIMIT
			? trimmed
			: trimmed.substring(0, QUESTION_SNAPSHOT_LIMIT);
	}

	private BigDecimal rate(long numerator, long denominator) {
		if (denominator <= 0L) {
			return BigDecimal.ZERO.setScale(2);
		}
		return BigDecimal.valueOf(numerator)
			.multiply(BigDecimal.valueOf(100))
			.divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
	}

	private String toJson(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			log.warn("LLM 사용량 source_breakdown 직렬화 실패: {}", e.getMessage());
			return null;
		}
	}

	public record SourceBreakdown(String type, int count) {
	}
}
