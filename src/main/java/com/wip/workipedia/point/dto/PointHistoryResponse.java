package com.wip.workipedia.point.dto;

import java.time.LocalDateTime;

public record PointHistoryResponse(
	long pointHistoryId,
	int pointAmount,
	String reasonType,
	String relatedType, // 포인트 이력 내용 ex) 게시글 작성 
	Long relatedId, // fk 아님 질문이면 question_id, 티켓이면  ticket_id
	LocalDateTime createdAt
) {
}
