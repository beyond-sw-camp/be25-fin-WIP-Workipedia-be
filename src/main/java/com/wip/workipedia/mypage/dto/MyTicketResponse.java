package com.wip.workipedia.mypage.dto;

import com.wip.workipedia.ticket.repository.MyTicketProjection;
import java.time.LocalDateTime;

public record MyTicketResponse(
	Long ticketId,
	String title,
	Long assignedDepartmentId,
	String assignedDepartmentName,
	String status,
	LocalDateTime createdAt
) {

	public static MyTicketResponse from(MyTicketProjection projection) {
		return new MyTicketResponse(
			projection.getTicketId(),
			projection.getTitle(),
			projection.getAssignedDepartmentId(),
			projection.getAssignedDepartmentName(),
			projection.getStatus(),
			projection.getCreatedAt()
		);
	}
}
