package com.wip.workipedia.mypage.dto;

import com.wip.workipedia.mypage.repository.MyTicketDetailProjection;
import java.time.LocalDateTime;

public record MyTicketDetailResponse(
	Long ticketId,
	String title,
	String content,
	Long assignedDepartmentId,
	String assignedDepartmentName,
	String status,
	Long remainingHours,
	boolean expired,
	boolean editable,
	boolean deletable,
	LocalDateTime createdAt,
	LocalDateTime completedAt,
	Answer answer
) {

	public record Answer(
		Long answerId,
		String content,
		Long authorId,
		String authorNickname,
		Long authorDepartmentId,
		String authorDepartmentName,
		LocalDateTime answeredAt
	) {
	}

	public static MyTicketDetailResponse from(
		MyTicketDetailProjection projection,
		long remainingHours,
		boolean expired,
		boolean editable,
		boolean deletable
	) {
		return new MyTicketDetailResponse(
			projection.getTicketId(),
			projection.getTitle(),
			projection.getContent(),
			projection.getAssignedDepartmentId(),
			projection.getAssignedDepartmentName(),
			projection.getStatus(),
			remainingHours,
			expired,
			editable,
			deletable,
			projection.getCreatedAt(),
			projection.getCompletedAt(),
			toAnswer(projection)
		);
	}

	private static Answer toAnswer(MyTicketDetailProjection projection) {
		if (projection.getAnswerId() == null) {
			return null;
		}

		return new Answer(
			projection.getAnswerId(),
			projection.getAnswerContent(),
			projection.getAnswerAuthorId(),
			projection.getAnswerAuthorNickname(),
			projection.getAnswerAuthorDepartmentId(),
			projection.getAnswerAuthorDepartmentName(),
			projection.getAnsweredAt()
		);
	}
}
