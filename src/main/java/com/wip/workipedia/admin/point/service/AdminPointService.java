package com.wip.workipedia.admin.point.service;

import com.wip.workipedia.admin.point.dto.AdminPointResponse;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.point.dto.MyPointResponse;
import com.wip.workipedia.point.service.PointService;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminPointService {
	// 포인트 이력에서 관리자 차감 이벤트를 구분하기 위한 사유 코드.
	private static final String ADMIN_DEDUCT_REASON_TYPE = "ADMIN_DEDUCT";
	// 포인트 이력의 관련 대상이 사용자임을 표시한다.
	private static final String USER_RELATED_TYPE = "USER";

	private final UserRepository userRepository;
	// 포인트 적립/차감 정책은 point 도메인의 기존 서비스를 재사용한다.
	private final PointService pointService;

	// 관리자 화면에서 사번으로 사용자 포인트를 검색한다.
	@Transactional(readOnly = true)
	public AdminPointResponse search(String employeeId) {
		User user = findUserByEmployeeId(employeeId);
		MyPointResponse point = pointService.getMyPoint(user.getUserId());

		return AdminPointResponse.of(user, point);
	}

	// 포인트 차감과 이력 저장은 PointService.spendPoint()에 위임한다.
	@Transactional
	public AdminPointResponse deduct(String employeeId, int amount) {
		User user = findUserByEmployeeId(employeeId);

		pointService.spendPoint(
			user.getUserId(),
			amount,
			ADMIN_DEDUCT_REASON_TYPE,
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
}
