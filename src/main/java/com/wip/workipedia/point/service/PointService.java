package com.wip.workipedia.point.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.esg.domain.EsgGrade;
import com.wip.workipedia.esg.repository.EsgGradeRepository;
import com.wip.workipedia.point.domain.PointHistory;
import com.wip.workipedia.point.domain.PointHistoryType;
import com.wip.workipedia.point.domain.PointReasonType;
import com.wip.workipedia.point.domain.PointsDailyLimit;
import com.wip.workipedia.point.domain.UserPoint;
import com.wip.workipedia.point.dto.MyPointResponse;
import com.wip.workipedia.point.dto.PointHistorySearchType;
import com.wip.workipedia.point.dto.PointHistoryResponse;
import com.wip.workipedia.point.repository.PointHistoryRepository;
import com.wip.workipedia.point.repository.PointsDailyLimitRepository;
import com.wip.workipedia.point.repository.UserPointRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {
	private static final int DAILY_EARN_LIMIT = 50;
	private static final int LOGIN_POINT = 1;
	private static final String USER_RELATED_TYPE = "USER";

	private final UserPointRepository userPointRepository;
	private final PointHistoryRepository pointHistoryRepository;
	private final PointsDailyLimitRepository pointsDailyLimitRepository;
	private final EsgGradeRepository esgGradeRepository;

	@Transactional(readOnly = true)
	public MyPointResponse getMyPoint(Long userId) {
		return userPointRepository.findByUserIdAndDeletedAtIsNull(userId)
			.map(MyPointResponse::from)
			.orElseGet(() -> new MyPointResponse(userId, 0L));
	}

	@Transactional(readOnly = true)
	public PageResponse<PointHistoryResponse> getMyPointHistory(
			Long userId,
			PointHistorySearchType type,
			Pageable pageable
	) {
		PointHistorySearchType searchType = type == null ? PointHistorySearchType.ALL : type;
		if (searchType == PointHistorySearchType.ALL) {
			return PageResponse.from(
				pointHistoryRepository
					.findByUserIdAndTypeNotAndDeletedAtIsNullOrderByCreatedAtDesc(
						userId,
						PointHistoryType.RESET,
						pageable
					)
					.map(PointHistoryResponse::from)
			);
		}

		return PageResponse.from(
			pointHistoryRepository
				.findByUserIdAndTypeAndDeletedAtIsNullOrderByCreatedAtDesc(
					userId,
					searchType.toHistoryType(),
					pageable
				)
				.map(PointHistoryResponse::from)
		);
	}

	// 사용자의 특정 포인트 적립 이력이 있는지 확인한다.
	@Transactional(readOnly = true)
	public boolean hasEarnedPoint(Long userId, PointReasonType reasonType) {
		return pointHistoryRepository.existsByUserIdAndReasonTypeAndTypeAndDeletedAtIsNull(
			userId, reasonType.name(), PointHistoryType.EARN);
	}

	@Transactional
	public void earnPoint(Long userId, int amount, PointReasonType reasonType, String relatedType, Long relatedId) {
		String reasonTypeName = reasonType.name();
		if (isDuplicateEarnEvent(reasonTypeName, relatedType, relatedId)) {
			return;
		}
		earnPointInternal(userId, amount, reasonTypeName, relatedType, relatedId);
	}

	// 일일 적립 한도를 초과하면 잔여 한도만큼만 적립하고, 한도가 없으면 아무 것도 저장하지 않는다.
	private void earnPointInternal(Long userId, int amount, String reasonType, String relatedType, Long relatedId) {
		int earnAmount = calculateEarnablePoint(userId, amount);
		if (earnAmount == 0) {
			return;
		}

		UserPoint userPoint = getOrCreateUserPoint(userId);
		userPoint.earn(earnAmount);
		updateEsgGrade(userPoint);
		pointHistoryRepository.save(PointHistory.earn(userId, earnAmount, reasonType, relatedType, relatedId));
	}

	// 로그인 포인트는 사용자별 하루 1회만 지급한다.
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void earnLoginPoint(Long userId) {
		if (hasEarnedLoginPointToday(userId)) {
			return;
		}
		earnPointInternal(userId, LOGIN_POINT, PointReasonType.LOGIN.name(), USER_RELATED_TYPE, userId);
	}

	@Transactional
	public void spendPoint(Long userId, int amount, String reasonType, String relatedType, Long relatedId) {
		UserPoint userPoint = getOrCreateUserPoint(userId);
		userPoint.spend(amount);
		pointHistoryRepository.save(PointHistory.spend(userId, amount, reasonType, relatedType, relatedId));
	}

	@Transactional
	public void resetPoint(Long userId, String reasonType, String relatedType, Long relatedId) {
		UserPoint userPoint = getOrCreateUserPoint(userId);
		long resetAmount = userPoint.reset();
		pointHistoryRepository.save(PointHistory.reset(userId, toNegativeInt(resetAmount),
			reasonType, relatedType, relatedId));
	}

	private UserPoint getOrCreateUserPoint(Long userId) {
		return userPointRepository.findByUserIdAndDeletedAtIsNull(userId)
			.orElseGet(() -> userPointRepository.save(UserPoint.create(userId)));
	}

	// 남은 한도보다 큰 포인트가 요청되면 잔여 한도만 적립한다.
	private int calculateEarnablePoint(Long userId, int requestedAmount) {
		if (requestedAmount <= 0) {
			throw new CustomException(ErrorType.POINT_INVALID_AMOUNT);
		}

		PointsDailyLimit dailyLimit = getOrCreateTodayDailyLimit(userId);
		long remainingPoint = dailyLimit.remainingPoint(DAILY_EARN_LIMIT);
		if (remainingPoint == 0) {
			return 0;
		}

		int earnAmount = Math.toIntExact(Math.min(requestedAmount, remainingPoint));
		dailyLimit.addPoint(earnAmount);
		return earnAmount;
	}

	// 기존 일일 한도 row에는 쓰기 락(Pessimistic Write Lock)을 걸어 같은 사용자의 동시 적립 요청이 일일 한도를 넘지 않게 한다.
	// 한 사용자의 포인트 적립 계산을 한 번에 한 요청만 하게 만드는 안전장치
	private PointsDailyLimit getOrCreateTodayDailyLimit(Long userId) {
		LocalDate today = LocalDate.now();
		pointsDailyLimitRepository.insertIgnore(userId, today);
		return pointsDailyLimitRepository.findActiveByUserIdAndPointDateForUpdate(userId, today)
			.orElseThrow(() -> new CustomException(ErrorType.INTERNAL_ERROR));
	}

	private void updateEsgGrade(UserPoint userPoint) {
		EsgGrade grade = esgGradeRepository.findActiveGradeByScore(userPoint.getEsgScore())
			.orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND));
		userPoint.updateGradeIfChanged(grade.getGradeId());
	}

	private boolean hasEarnedLoginPointToday(Long userId) {
		LocalDate today = LocalDate.now();
		LocalDateTime startAt = today.atStartOfDay();
		LocalDateTime endAt = today.plusDays(1).atStartOfDay();
		return pointHistoryRepository
			.existsByUserIdAndReasonTypeAndRelatedTypeAndRelatedIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanAndDeletedAtIsNull(
				userId,
				PointReasonType.LOGIN.name(),
				USER_RELATED_TYPE,
				userId,
				startAt,
				endAt
			);
	}

	// 관련 엔티티가 없는 적립은 이벤트 멱등성을 판단할 수 없어 중복 검사 대상에서 제외한다.
	private boolean isDuplicateEarnEvent(String reasonType, String relatedType, Long relatedId) {
		if (reasonType == null || relatedType == null || relatedId == null) {
			return false;
		}
		return pointHistoryRepository.existsByReasonTypeAndRelatedTypeAndRelatedIdAndTypeAndDeletedAtIsNull(
			reasonType,
			relatedType,
			relatedId,
			PointHistoryType.EARN
		);
	}

	private int toNegativeInt(long amount) {
		try {
			return Math.negateExact(Math.toIntExact(amount));
		} catch (ArithmeticException exception) {
			throw new CustomException(ErrorType.POINT_AMOUNT_OVERFLOW);
		}
	}

	// 매년 1월 1일에 모든 사용자의 현재 포인트와 ESG 점수를 초기화하고 RESET 이력을 남긴다.
	@Transactional
	public void resetAllUserPointsForNewYear() {
		userPointRepository.findByDeletedAtIsNull()
				.forEach(userPoint ->
						resetPoint(
								userPoint.getUserId(),
								"YEARLY_RESET",
								"USER",
								userPoint.getUserId()
						)
				);
	}
}
