package com.wip.workipedia.mypage.repository;

import java.time.LocalDateTime;

// 마이페이지 내 발행 티켓 목록 조회 시 tickets와 departments 조인 결과를 담는 조회 전용 Projection입니다.
public interface MyPageTicketProjection {

	Long getTicketId();

	String getTitle();

	Long getAssignedDepartmentId();

	String getAssignedDepartmentName();

	String getStatus();

	LocalDateTime getCreatedAt();
}
