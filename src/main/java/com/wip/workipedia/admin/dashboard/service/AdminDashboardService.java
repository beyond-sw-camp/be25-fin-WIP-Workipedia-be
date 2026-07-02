package com.wip.workipedia.admin.dashboard.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.admin.dashboard.dto.DepartmentAutoAssignmentRateResponse;
import com.wip.workipedia.admin.dashboard.dto.DepartmentAutoAssignmentRateResponse.DepartmentAutoAssignmentRateItem;
import com.wip.workipedia.admin.dashboard.dto.DepartmentTicketStatusResponse;
import com.wip.workipedia.admin.dashboard.dto.DepartmentTicketStatusResponse.DepartmentTicketStatusItem;
import com.wip.workipedia.admin.dashboard.dto.LlmUsageSavingsResponse;
import com.wip.workipedia.admin.dashboard.dto.LlmUsageSavingsResponse.RecentSample;
import com.wip.workipedia.admin.dashboard.dto.LlmUsageSavingsResponse.SourceBreakdown;
import com.wip.workipedia.admin.dashboard.dto.LlmUsageSavingsResponse.Summary;
import com.wip.workipedia.admin.dashboard.dto.MonthlyAutoAssignmentRateResponse;
import com.wip.workipedia.admin.dashboard.dto.MonthlyAutoAssignmentRateResponse.MonthlyAutoAssignmentRatePoint;
import com.wip.workipedia.admin.dashboard.dto.MonthlyTicketTrendResponse;
import com.wip.workipedia.admin.dashboard.dto.MonthlyTicketTrendResponse.MonthlyTicketTrendPoint;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.department.repository.DepartmentRepository;
import com.wip.workipedia.llmusage.domain.LlmUsageMetric;
import com.wip.workipedia.llmusage.repository.LlmUsageMetricRepository;
import com.wip.workipedia.ticket.domain.TicketStatus;
import com.wip.workipedia.ticket.repository.TicketRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

	private static final int DEFAULT_MONTHS = 6;
	private static final int MIN_MONTHS = 1;
	private static final int MAX_MONTHS = 12;
	private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

	private final TicketRepository ticketRepository;
	private final DepartmentRepository departmentRepository;
	private final LlmUsageMetricRepository llmUsageMetricRepository;
	private final ObjectMapper objectMapper;

	public MonthlyAutoAssignmentRateResponse getMonthlyAutoAssignmentRate(Integer months) {
		int normalizedMonths = normalizeMonths(months);
		Range range = range(normalizedMonths);
		Map<String, TicketRepository.MonthlyAutoAssignmentRateProjection> rates =
			ticketRepository.countMonthlyAutoAssignmentRate(range.startAt(), range.endAt())
				.stream()
				.collect(Collectors.toMap(
					TicketRepository.MonthlyAutoAssignmentRateProjection::getMonth,
					Function.identity()
				));

		List<MonthlyAutoAssignmentRatePoint> points = months(normalizedMonths)
			.stream()
			.map(month -> {
				String key = month.format(MONTH_FORMATTER);
				TicketRepository.MonthlyAutoAssignmentRateProjection rate = rates.get(key);
				long totalCount = rate == null ? 0L : rate.getTotalTicketCount();
				long autoAssignedCount = rate == null ? 0L : rate.getAutoAssignedTicketCount();
				return new MonthlyAutoAssignmentRatePoint(
					key,
					totalCount,
					autoAssignedCount,
					rate(autoAssignedCount, totalCount)
				);
			})
			.toList();

		return new MonthlyAutoAssignmentRateResponse(normalizedMonths, points);
	}

	public MonthlyTicketTrendResponse getMonthlyTicketTrend(Integer months) {
		int normalizedMonths = normalizeMonths(months);
		Range range = range(normalizedMonths);
		Map<String, Long> counts = ticketRepository.countMonthlyIssuedTickets(range.startAt(), range.endAt())
			.stream()
			.collect(Collectors.toMap(
				TicketRepository.MonthlyCountProjection::getMonth,
				TicketRepository.MonthlyCountProjection::getCount
			));

		List<MonthlyTicketTrendPoint> points = months(normalizedMonths)
			.stream()
			.map(month -> {
				String key = month.format(MONTH_FORMATTER);
				return new MonthlyTicketTrendPoint(key, counts.getOrDefault(key, 0L));
			})
			.toList();

		return new MonthlyTicketTrendResponse(normalizedMonths, points);
	}

	public DepartmentTicketStatusResponse getDepartmentTicketStatus() {
		List<Department> departments = departmentRepository.findActiveDepartments();
		Map<Long, Map<TicketStatus, Long>> countsByDepartment = new java.util.HashMap<>();
		ticketRepository.countActiveTicketsByDepartmentAndStatus()
			.forEach(projection -> countsByDepartment
				.computeIfAbsent(projection.getDepartmentId(), key -> new EnumMap<>(TicketStatus.class))
				.put(projection.getStatus(), projection.getCount()));

		List<DepartmentTicketStatusItem> items = departments.stream()
			.map(department -> {
				Map<TicketStatus, Long> counts = countsByDepartment.getOrDefault(
					department.getDepartmentId(),
					Map.of()
				);
				long assignedCount = counts.getOrDefault(TicketStatus.ASSIGNED, 0L);
				long completedCount = counts.getOrDefault(TicketStatus.COMPLETED, 0L);
				return new DepartmentTicketStatusItem(
					department.getDepartmentId(),
					department.getDepartmentName(),
					assignedCount + completedCount,
					assignedCount,
					completedCount
				);
			})
			.toList();

		return new DepartmentTicketStatusResponse(items);
	}

	public DepartmentAutoAssignmentRateResponse getDepartmentAutoAssignmentRate() {
		List<Department> departments = departmentRepository.findActiveDepartments();
		Map<Long, TicketRepository.DepartmentAutoAssignmentRateProjection> rates =
			ticketRepository.countDepartmentAutoAssignmentRate()
				.stream()
				.collect(Collectors.toMap(
					TicketRepository.DepartmentAutoAssignmentRateProjection::getDepartmentId,
					Function.identity()
				));

		List<DepartmentAutoAssignmentRateItem> items = departments.stream()
			.map(department -> {
				TicketRepository.DepartmentAutoAssignmentRateProjection projection = rates.get(department.getDepartmentId());
				long totalCount = projection == null ? 0L : projection.getTotalTicketCount();
				long autoAssignedCount = projection == null ? 0L : projection.getAutoAssignedTicketCount();
				return new DepartmentAutoAssignmentRateItem(
					department.getDepartmentId(),
					department.getDepartmentName(),
					totalCount,
					autoAssignedCount,
					rate(autoAssignedCount, totalCount)
				);
			})
			.toList();

		return new DepartmentAutoAssignmentRateResponse(items);
	}

	public LlmUsageSavingsResponse getLlmUsageSavings() {
		LlmUsageMetricRepository.LlmUsageSummaryProjection totals =
			llmUsageMetricRepository.summarizeActive();
		long sampleCount = value(totals.getSampleCount());
		long fullFileTokens = value(totals.getFullFileTokens());
		long ragTokens = value(totals.getRagTokens());
		long savedTokens = value(totals.getSavedTokens());

		Summary summary = new Summary(
			sampleCount,
			fullFileTokens,
			ragTokens,
			savedTokens,
			percent(savedTokens, fullFileTokens),
			value(totals.getFullFileCredits()),
			value(totals.getRagCredits()),
			value(totals.getSavedCredits()),
			value(totals.getFullFileCalls()),
			value(totals.getRagCalls()),
			value(totals.getSavedCalls()),
			value(totals.getSourceCount()),
			value(totals.getCitedChunkCount()),
			average(fullFileTokens, sampleCount),
			average(ragTokens, sampleCount)
		);

		List<RecentSample> recentSamples = llmUsageMetricRepository
			.findByIsDeletedOrderByCreatedAtDesc("N", PageRequest.of(0, 5))
			.stream()
			.map(this::toRecentSample)
			.toList();

		return new LlmUsageSavingsResponse(
			summary,
			recentSamples,
			totalSourceBreakdown()
		);
	}

	private int normalizeMonths(Integer months) {
		if (months == null) {
			return DEFAULT_MONTHS;
		}
		if (months < MIN_MONTHS || months > MAX_MONTHS) {
			throw new CustomException(ErrorType.BAD_REQUEST, "months must be between 1 and 12.");
		}
		return months;
	}

	private Range range(int months) {
		YearMonth endMonth = YearMonth.from(LocalDate.now()).plusMonths(1);
		YearMonth startMonth = endMonth.minusMonths(months);
		return new Range(startMonth.atDay(1).atStartOfDay(), endMonth.atDay(1).atStartOfDay());
	}

	private List<YearMonth> months(int months) {
		YearMonth firstMonth = YearMonth.from(LocalDate.now()).minusMonths(months - 1L);
		return IntStream.range(0, months)
			.mapToObj(firstMonth::plusMonths)
			.toList();
	}

	private BigDecimal rate(long numerator, long denominator) {
		if (denominator == 0L) {
			return BigDecimal.ZERO.setScale(2);
		}
		return BigDecimal.valueOf(numerator)
			.multiply(BigDecimal.valueOf(100))
			.divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
	}

	private RecentSample toRecentSample(LlmUsageMetric metric) {
		return new RecentSample(
			metric.getLlmUsageMetricId(),
			metric.getChatbotMessageId(),
			metric.getCreatedAt(),
			metric.getQuestionSnapshot(),
			"Y".equals(metric.getAnswerable()),
			metric.getSourceCount(),
			metric.getCitedChunkCount(),
			metric.getFullFileTokens(),
			metric.getRagTokens(),
			metric.getSavedTokens(),
			percent(metric.getSavedTokens(), metric.getFullFileTokens()),
			metric.getFullFileCredits(),
			metric.getRagCredits(),
			metric.getSavedCredits(),
			metric.getFullFileCalls(),
			metric.getRagCalls(),
			metric.getSavedCalls(),
			parseSourceBreakdown(metric.getSourceBreakdown())
		);
	}

	private List<SourceBreakdown> totalSourceBreakdown() {
		Map<String, Integer> totals = new LinkedHashMap<>();
		for (String raw : llmUsageMetricRepository.findActiveSourceBreakdowns()) {
			for (SourceBreakdown item : parseSourceBreakdown(raw)) {
				totals.put(item.type(), totals.getOrDefault(item.type(), 0) + item.count());
			}
		}
		return totals.entrySet().stream()
			.map(entry -> new SourceBreakdown(entry.getKey(), entry.getValue()))
			.sorted((a, b) -> Integer.compare(b.count(), a.count()))
			.toList();
	}

	private List<SourceBreakdown> parseSourceBreakdown(String raw) {
		if (raw == null || raw.isBlank()) {
			return List.of();
		}
		try {
			return objectMapper.readValue(raw, new TypeReference<>() {});
		} catch (JsonProcessingException e) {
			log.warn("LLM 사용량 source_breakdown 파싱 실패: {}", e.getMessage());
			return List.of();
		}
	}

	private long value(Long value) {
		return value == null ? 0L : value;
	}

	private long average(long total, long count) {
		return count == 0L ? 0L : Math.round((double) total / count);
	}

	private long percent(long numerator, long denominator) {
		if (denominator <= 0L) {
			return 0L;
		}
		return Math.round((double) numerator * 100 / denominator);
	}

	private record Range(LocalDateTime startAt, LocalDateTime endAt) {
	}
}
