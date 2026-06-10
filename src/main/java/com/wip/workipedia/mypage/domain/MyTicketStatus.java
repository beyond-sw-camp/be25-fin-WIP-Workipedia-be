package com.wip.workipedia.mypage.domain;

import com.wip.workipedia.ticket.domain.TicketStatus;
import java.util.List;

public enum MyTicketStatus {
	WAITING(List.of(
		TicketStatus.RECEIVED,
		TicketStatus.COMMON_QUEUE,
		TicketStatus.ASSIGNED
	)),
	COMPLETED(List.of(TicketStatus.COMPLETED));

	private final List<TicketStatus> ticketStatuses;

	MyTicketStatus(List<TicketStatus> ticketStatuses) {
		this.ticketStatuses = ticketStatuses;
	}

	// 화면 탭 기준 상태값을 실제 tickets.status 조회 조건으로 변환합니다.
	public List<TicketStatus> getTicketStatuses() {
		return ticketStatuses;
	}
}
