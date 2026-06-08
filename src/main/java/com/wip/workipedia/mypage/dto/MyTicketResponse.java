package com.wip.workipedia.mypage.dto;

import com.wip.workipedia.ticket.domain.Ticket;
import java.time.LocalDateTime;

public record MyTicketResponse(
	Long ticketId,
	String title,
	Long assignedDepartmentId,
	String assignedDepartmentName,
	String status,
	LocalDateTime createdAt
) {

	public static MyTicketResponse from(Ticket ticket, String assignedDepartmentName) {
		return new MyTicketResponse(
			ticket.getTicketId(),
			ticket.getTitle(),
			ticket.getAssignedDepartmentId(),
			assignedDepartmentName,
			ticket.getStatus().name(),
			ticket.getCreatedAt()
		);
	}
}
