package com.wip.workipedia.mypage.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.esg.domain.EsgGrade;
import com.wip.workipedia.esg.repository.EsgGradeRepository;
import com.wip.workipedia.mypage.domain.MyTicketStatus;
import com.wip.workipedia.mypage.dto.MyPageResponse;
import com.wip.workipedia.mypage.dto.MyTicketResponse;
import com.wip.workipedia.mypage.repository.MyPageTicketProjection;
import com.wip.workipedia.mypage.repository.MyPageTicketRepository;
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

	private final UserRepository userRepository;
	private final TicketRepository ticketRepository;
	private final MyPageTicketRepository myPageTicketRepository;
	private final UserPointRepository userPointRepository;
	private final EsgGradeRepository esgGradeRepository;
	private final NotificationSettingRepository notificationSettingRepository;

	// 로그인 사용자 기준으로 마이페이지 화면에 필요한 모든 데이터를 조회해 응답을 조립합니다.
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

	// 로그인 사용자가 발행한 티켓 목록을 화면 탭 상태에 맞게 조회합니다.
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

	// 티켓 생성 시각 기준 48시간까지의 남은 시간과 만료 여부를 계산해 응답 DTO를 생성합니다.
	private MyTicketResponse toMyTicketResponse(MyPageTicketProjection projection) {
		LocalDateTime deadline = projection.getCreatedAt().plusHours(48);
		LocalDateTime now = LocalDateTime.now();
		boolean expired = !now.isBefore(deadline);
		long remainingHours = expired ? 0L : Duration.between(now, deadline).toHours();

		return MyTicketResponse.from(projection, remainingHours, expired);
	}

	// 사용자 포인트 정보의 gradeId를 기준으로 현재 ESG 등급을 찾고, 포인트 정보가 없으면 첫 등급을 기본값으로 사용합니다.
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

	// User 엔티티에서 마이페이지 상단 인사말에 필요한 사용자 요약 정보를 생성합니다.
	private MyPageResponse.UserSummary toUserSummary(User user) {
		return new MyPageResponse.UserSummary(
			user.getUserId(),
			user.getNickname(),
			user.getRole().name(),
			user.getStatus().name()
		);
	}

	// 저장된 알림 설정 엔티티를 마이페이지 응답용 알림 설정 DTO로 변환합니다.
	private MyPageResponse.NotificationSettings toNotificationSettings(NotificationSetting notificationSetting) {
		return new MyPageResponse.NotificationSettings(
			notificationSetting.isAllEnabled(),
			notificationSetting.isTicketEnabled(),
			notificationSetting.isWorkiEnabled(),
			notificationSetting.isManualEnabled()
		);
	}

	// 현재 ESG 등급 엔티티와 ESG 점수를 조합해 ESG 현황 화면에 필요한 요약 정보를 생성합니다.
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

	// 전체 ESG 등급 목록을 마이페이지의 등급 진행 구간 응답으로 변환합니다.
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

	// 현재 ESG 점수와 현재 등급의 최대 점수를 비교해 다음 등급까지 남은 점수를 계산합니다.
	private Long calculateRemainingScore(
		EsgGrade esgGrade,
		long esgScore
	) {
		if (esgGrade.getMaxScore() == null) {
			return null;
		}

		return Math.max(esgGrade.getMaxScore() - esgScore, 0L);
	}
}
