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
	LocalDateTime createdAt
) {

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
			projection.getCreatedAt()
		);
	}
}
