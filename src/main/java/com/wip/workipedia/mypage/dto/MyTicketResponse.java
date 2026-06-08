package com.wip.workipedia.mypage.dto;

import com.wip.workipedia.ticket.domain.Ticket;
import java.time.LocalDateTime;

public record MyTicketResponse(
	Long ticketId,
	String title,
	String status,
	String statusLabel,
	LocalDateTime createdAt
) {

	public static MyTicketResponse from(Ticket ticket) {
		return new MyTicketResponse(
			ticket.getTicketId(),
			ticket.getTitle(),
			ticket.getStatus().name(),
			createStatusLabel(ticket),
			ticket.getCreatedAt()
		);
	}

	private static String createStatusLabel(Ticket ticket) {
		return switch (ticket.getStatus()) {
			case COMPLETED -> "답변 완료";
			default -> "답변 대기";
		};
	}
}
