package com.wip.workipedia.ticket.dto;

// AI가 정리한 티켓 초안. FE 폼에 채워지며 사용자가 수정할 수 있다.
public record TicketDraftResponse(
	String title,
	String content
) {
}
