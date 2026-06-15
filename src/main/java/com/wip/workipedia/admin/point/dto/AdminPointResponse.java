package com.wip.workipedia.admin.point.dto;

import com.wip.workipedia.point.dto.MyPointResponse;
import com.wip.workipedia.user.domain.User;

// 관리자 포인트 조회/차감 API의 응답값.
public record AdminPointResponse(
	Long userId,
	String employeeId,
	String nickname,
	long currentPoint
) {
	// 사용자 기본 정보와 포인트 도메인 응답을 관리자 응답 형태로 합친다.
	public static AdminPointResponse of(User user, MyPointResponse point) {
		return of(user, point.currentPoint());
	}

	public static AdminPointResponse of(User user, long currentPoint) {
		return new AdminPointResponse(
			user.getUserId(),
			user.getEmployeeId(),
			user.getNickname(),
			currentPoint
		);
	}
}
