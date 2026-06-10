package com.wip.workipedia.mypage.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.esg.domain.EsgGrade;
import com.wip.workipedia.esg.repository.EsgGradeRepository;
import com.wip.workipedia.mypage.domain.MyTicketStatus;
import com.wip.workipedia.mypage.dto.MyPageResponse;
import com.wip.workipedia.mypage.dto.MyTicketDetailResponse;
import com.wip.workipedia.mypage.dto.MyTicketResponse;
import com.wip.workipedia.mypage.repository.MyPageTicketProjection;
import com.wip.workipedia.mypage.repository.MyPageTicketRepository;
import com.wip.workipedia.mypage.repository.MyTicketDetailProjection;
import com.wip.workipedia.notification.domain.NotificationSetting;
import com.wip.workipedia.notification.repository.NotificationSettingRepository;
import com.wip.workipedia.point.domain.UserPoint;
import com.wip.workipedia.point.repository.UserPointRepository;
import com.wip.workipedia.ticket.domain.TicketStatus;
import com.wip.workipedia.ticket.repository.TicketRepository;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

	private static final int TICKET_RESPONSE_DEADLINE_HOURS = 48;
	private static final boolean MY_TICKET_EDITABLE = false;
	private static final boolean MY_TICKET_DELETABLE = false;

	private final UserRepository userRepository;
	private final TicketRepository ticketRepository;
	private final MyPageTicketRepository myPageTicketRepository;
	private final UserPointRepository userPointRepository;
	private final EsgGradeRepository esgGradeRepository;
	private final NotificationSettingRepository notificationSettingRepository;

	public MyPageResponse getMyPage(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND));
		UserPoint userPoint = userPointRepository.findByUserIdAndDeletedAtIsNull(userId)
			.orElse(null);
		long createdTicketCount = ticketRepository.countByRequesterIdAndDeletedAtIsNull(userId);
		List<EsgGrade> esgGrades = esgGradeRepository.findByDeletedAtIsNullOrderByMinScoreAsc();
		EsgGrade currentEsgGrade = findCurrentEsgGrade(userPoint, esgGrades);
		NotificationSetting notificationSetting = notificationSettingRepository.findByUserIdAndDeletedAtIsNull(userId)
			.orElseGet(() -> NotificationSetting.createDefault(userId));

		long currentPoint = userPoint == null ? 0L : userPoint.getCurrentPoint();
		long esgScore = userPoint == null ? 0L : userPoint.getEsgScore();

		return new MyPageResponse(
			toUserSummary(user),
			new MyPageResponse.TicketSummary(createdTicketCount),
			new MyPageResponse.PointSummary(currentPoint, esgScore),
			toNotificationSettings(notificationSetting),
			toEsgGradeSummary(currentEsgGrade, esgScore),
			toEsgGradeProgress(esgGrades)
		);
	}

	public PageResponse<MyTicketResponse> getMyTickets(
		Long userId,
		MyTicketStatus status,
		Pageable pageable
	) {
		MyTicketStatus myTicketStatus = status == null ? MyTicketStatus.WAITING : status;
		List<String> statuses = myTicketStatus.getTicketStatuses().stream()
			.map(TicketStatus::name)
			.toList();

		return PageResponse.from(myPageTicketRepository.findMyTickets(userId, statuses, pageable)
			.map(this::toMyTicketResponse));
	}

	public MyTicketDetailResponse getMyTicketDetail(Long userId, Long ticketId) {
		MyTicketDetailProjection projection = myPageTicketRepository.findMyTicketDetail(userId, ticketId)
			.orElseThrow(() -> new CustomException(ErrorType.TICKET_NOT_FOUND));
		TicketTimeStatus ticketTimeStatus = calculateTicketTimeStatus(projection.getAssignedAt());

		return MyTicketDetailResponse.from(
			projection,
			ticketTimeStatus.remainingHours(),
			ticketTimeStatus.expired(),
			MY_TICKET_EDITABLE,
			MY_TICKET_DELETABLE
		);
	}

	private MyTicketResponse toMyTicketResponse(MyPageTicketProjection projection) {
		TicketTimeStatus ticketTimeStatus = calculateTicketTimeStatus(projection.getAssignedAt());

		return MyTicketResponse.from(projection, ticketTimeStatus.remainingHours(), ticketTimeStatus.expired());
	}

	private TicketTimeStatus calculateTicketTimeStatus(LocalDateTime assignedAt) {
		if (assignedAt == null) {
			return new TicketTimeStatus(TICKET_RESPONSE_DEADLINE_HOURS, false);
		}

		LocalDateTime deadline = assignedAt.plusHours(TICKET_RESPONSE_DEADLINE_HOURS);
		LocalDateTime now = LocalDateTime.now();
		boolean expired = !now.isBefore(deadline);
		long remainingHours = expired ? 0L : Duration.between(now, deadline).toHours();

		return new TicketTimeStatus(remainingHours, expired);
	}

	private EsgGrade findCurrentEsgGrade(
		UserPoint userPoint,
		List<EsgGrade> esgGrades
	) {
		if (esgGrades.isEmpty()) {
			throw new CustomException(ErrorType.NOT_FOUND);
		}

		if (userPoint == null) {
			return esgGrades.get(0);
		}

		return esgGrades.stream()
			.filter(esgGrade -> esgGrade.getGradeId().equals(userPoint.getGradeId()))
			.findFirst()
			.orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND));
	}

	private MyPageResponse.UserSummary toUserSummary(User user) {
		return new MyPageResponse.UserSummary(
			user.getUserId(),
			user.getNickname(),
			user.getRole().name(),
			user.getStatus().name()
		);
	}

	private MyPageResponse.NotificationSettings toNotificationSettings(NotificationSetting notificationSetting) {
		return new MyPageResponse.NotificationSettings(
			notificationSetting.isAllEnabled(),
			notificationSetting.isTicketEnabled(),
			notificationSetting.isWorkiEnabled(),
			notificationSetting.isManualEnabled()
		);
	}

	private MyPageResponse.EsgGradeSummary toEsgGradeSummary(
		EsgGrade esgGrade,
		long esgScore
	) {
		return new MyPageResponse.EsgGradeSummary(
			esgGrade.getGradeId(),
			esgGrade.getGradeName(),
			esgGrade.getGradeImageUrl(),
			esgScore,
			esgGrade.getMinScore(),
			esgGrade.getMaxScore(),
			calculateRemainingScore(esgGrade, esgScore)
		);
	}

	private List<MyPageResponse.EsgGradeProgress> toEsgGradeProgress(List<EsgGrade> esgGrades) {
		return esgGrades.stream()
			.map(esgGrade -> new MyPageResponse.EsgGradeProgress(
				esgGrade.getGradeId(),
				esgGrade.getGradeName(),
				esgGrade.getMinScore(),
				esgGrade.getMaxScore()
			))
			.toList();
	}

	private Long calculateRemainingScore(
		EsgGrade esgGrade,
		long esgScore
	) {
		if (esgGrade.getMaxScore() == null) {
			return null;
		}

		return Math.max(esgGrade.getMaxScore() - esgScore, 0L);
	}

	private record TicketTimeStatus(long remainingHours, boolean expired) {
	}
}
