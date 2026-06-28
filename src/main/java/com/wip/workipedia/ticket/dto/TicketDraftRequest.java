package com.wip.workipedia.ticket.dto;

import jakarta.validation.constraints.NotBlank;

// 사용자가 자유롭게 입력한 요청 원문. AI가 티켓 초안(제목/내용)으로 정리한다.
public record TicketDraftRequest(
	@NotBlank String rawText
) {
}
