package com.wip.workipedia.admin.point.service;

import com.wip.workipedia.admin.point.dto.AdminPointResponse;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.point.domain.UserPoint;
import com.wip.workipedia.point.dto.MyPointResponse;
import com.wip.workipedia.point.repository.UserPointRepository;
import com.wip.workipedia.point.service.PointService;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminPointService {

	// 포인트 이력의 관련 대상이 사용자임을 표시한다.
	private static final String USER_RELATED_TYPE = "USER";

	private final UserRepository userRepository;
	private final UserPointRepository userPointRepository;
	// 포인트 적립/차감 정책은 point 도메인의 기존 서비스를 재사용한다.
	private final PointService pointService;

	// 관리자 화면에서 전체 사용자 포인트 목록을 조회한다.
	@Transactional(readOnly = true)
	public PageResponse<AdminPointResponse> findAll(Pageable pageable) {
		Page<User> users = userRepository.findByDeletedAtIsNull(pageable);
		Map<Long, UserPoint> pointMap = findUserPointMap(users.getContent());

		return PageResponse.from(users.map(user -> AdminPointResponse.of(
			user,
			currentPoint(pointMap, user.getUserId())
		)));
	}

	// 관리자 화면에서 사번으로 사용자 포인트를 검색한다.
	@Transactional(readOnly = true)
	public AdminPointResponse search(String employeeId) {
		User user = findUserByEmployeeId(employeeId);
		MyPointResponse point = pointService.getMyPoint(user.getUserId());

		return AdminPointResponse.of(user, point);
	}

	// 포인트 차감과 이력 저장은 PointService.spendPoint()에 위임한다.
	@Transactional
	public AdminPointResponse deduct(String employeeId, int amount, String reason) {
		User user = findUserByEmployeeId(employeeId);

		pointService.spendPoint(
			user.getUserId(),
			amount,
			reason,
			USER_RELATED_TYPE,
			user.getUserId()
		);

		MyPointResponse point = pointService.getMyPoint(user.getUserId());
		return AdminPointResponse.of(user, point);
	}

	// 관리자 API는 userId가 아니라 사번으로 사용자를 식별한다.
	private User findUserByEmployeeId(String employeeId) {
		return userRepository.findByEmployeeId(employeeId)
			.orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다."));
	}

	// 현재 페이지에 포함된 사용자들의 포인트를 한 번에 조회해 목록 조회 시 N+1을 피한다.
	private Map<Long, UserPoint> findUserPointMap(List<User> users) {
		if (users.isEmpty()) {
			return Map.of();
		}

		List<Long> userIds = users.stream()
			.map(User::getUserId)
			.toList();

		return userPointRepository.findByUserIdInAndDeletedAtIsNull(userIds)
			.stream()
			.collect(Collectors.toMap(UserPoint::getUserId, Function.identity()));
	}

	// 포인트 row가 아직 없는 사용자는 현재 포인트를 0으로 보여준다.
	private long currentPoint(Map<Long, UserPoint> pointMap, Long userId) {
		UserPoint userPoint = pointMap.get(userId);
		return userPoint == null ? 0 : userPoint.getCurrentPoint();
	}
}
