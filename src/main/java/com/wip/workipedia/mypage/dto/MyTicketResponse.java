package com.wip.workipedia.mypage.dto;

import com.wip.workipedia.mypage.repository.MyPageTicketProjection;

import java.time.LocalDateTime;

public record MyTicketResponse(
	Long ticketId,
	String title,
	Long assignedDepartmentId,
	String assignedDepartmentName,
	String status,
	Long remainingHours,
	boolean expired,
	LocalDateTime assignedAt,
	LocalDateTime createdAt
) {

	public static MyTicketResponse from(
		MyPageTicketProjection projection,
		Long remainingHours,
		boolean expired
	) {
		return new MyTicketResponse(
				projection.getTicketId(),
				projection.getTitle(),
				projection.getAssignedDepartmentId(),
				projection.getAssignedDepartmentName(),
				projection.getStatus(),
				remainingHours,
				expired,
				projection.getAssignedAt(),
				projection.getCreatedAt()
		);
	}
}
