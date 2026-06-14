package com.wip.workipedia.admin.team.dashboard.service;

import com.wip.workipedia.admin.team.dashboard.dto.MonthlyTrendResponse;
import com.wip.workipedia.admin.team.dashboard.dto.MonthlyTrendResponse.MonthlyPoint;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.knowledge.repository.KnowledgeDataRepository;
import com.wip.workipedia.ticket.repository.TicketRepository;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.domain.UserRole;
import com.wip.workipedia.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamAdminDashboardService {

	private static final int DEFAULT_MONTHS = 6;
	private static final int MIN_MONTHS = 1;
	private static final int MAX_MONTHS = 12;
	private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

	private final KnowledgeDataRepository knowledgeDataRepository;
	private final TicketRepository ticketRepository;
	private final UserRepository userRepository;

	public MonthlyTrendResponse getKnowledgeTrend(Long actorUserId, Integer months) {
		User actor = getTeamAdmin(actorUserId);
		int normalizedMonths = normalizeMonths(months);
		Range range = range(normalizedMonths);
		Map<String, Long> counts = knowledgeDataRepository.countMonthlyApprovedByDepartment(
				actor.getDepartment().getDepartmentId(),
				range.startAt(),
				range.endAt()
			)
			.stream()
			.collect(Collectors.toMap(
				KnowledgeDataRepository.MonthlyCountProjection::getMonth,
				KnowledgeDataRepository.MonthlyCountProjection::getCount
			));
		return trendResponse(actor, normalizedMonths, counts);
	}

	public MonthlyTrendResponse getChatbotAssignmentTrend(Long actorUserId, Integer months) {
		User actor = getTeamAdmin(actorUserId);
		int normalizedMonths = normalizeMonths(months);
		Range range = range(normalizedMonths);
		Map<String, Long> counts = ticketRepository.countMonthlyAutoAssignedByDepartment(
				actor.getDepartment().getDepartmentId(),
				range.startAt(),
				range.endAt()
			)
			.stream()
			.collect(Collectors.toMap(
				TicketRepository.MonthlyCountProjection::getMonth,
				TicketRepository.MonthlyCountProjection::getCount
			));
		return trendResponse(actor, normalizedMonths, counts);
	}

	private User getTeamAdmin(Long actorUserId) {
		User user = userRepository.findById(actorUserId)
			.orElseThrow(() -> new CustomException(ErrorType.KNOWLEDGE_DATA_FORBIDDEN));
		if (user.getRole() != UserRole.TEAM_ADMIN || user.getDepartment() == null) {
			throw new CustomException(ErrorType.KNOWLEDGE_DATA_FORBIDDEN);
		}
		return user;
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

	private MonthlyTrendResponse trendResponse(User actor, int months, Map<String, Long> counts) {
		YearMonth firstMonth = YearMonth.from(LocalDate.now()).minusMonths(months - 1L);
		List<MonthlyPoint> points = IntStream.range(0, months)
			.mapToObj(firstMonth::plusMonths)
			.map(month -> {
				String key = month.format(MONTH_FORMATTER);
				return new MonthlyPoint(key, counts.getOrDefault(key, 0L));
			})
			.toList();

		return new MonthlyTrendResponse(
			actor.getDepartment().getDepartmentId(),
			actor.getDepartment().getDepartmentName(),
			months,
			points
		);
	}

	private record Range(LocalDateTime startAt, LocalDateTime endAt) {
	}
}
