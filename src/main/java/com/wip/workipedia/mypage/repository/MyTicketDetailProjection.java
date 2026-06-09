package com.wip.workipedia.mypage.repository;

import java.time.LocalDateTime;

// 내 발행 티켓 상세 조회 시 tickets와 departments 조인 결과를 담는 조회 전용 Projection입니다.
public interface MyTicketDetailProjection {

	Long getTicketId();

	String getTitle();

	String getContent();

	Long getAssignedDepartmentId();

	String getAssignedDepartmentName();

	String getStatus();

	LocalDateTime getCreatedAt();

	LocalDateTime getCompletedAt();

	Long getAnswerId();

	String getAnswerContent();

	Long getAnswerAuthorId();

	String getAnswerAuthorNickname();

	Long getAnswerAuthorDepartmentId();

	String getAnswerAuthorDepartmentName();

	LocalDateTime getAnsweredAt();
}
