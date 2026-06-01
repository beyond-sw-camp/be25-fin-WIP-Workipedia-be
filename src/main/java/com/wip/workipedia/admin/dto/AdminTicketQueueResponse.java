package com.wip.workipedia.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 팀 큐와 공통 접수 큐에서 공통으로 사용하는 티켓 목록 응답
public record AdminTicketQueueResponse(
	long ticketId,
	String title,
	String status,
	Long assignedDepartmentId,
	String assignedDepartmentName,
	// 80 이상은 자동 배정, 80 미만은 관리자 검토 대상
	// 50 미만은 공통 접수 큐 또는 추가 정보 요청 기준으로 사용
	BigDecimal routingConfidenceScore, //자동 부서 배정 신뢰도
	String routingDecision, //라우팅 결과 결정값
	LocalDateTime createdAt
) {
}

// routing_confidence_score =
//   keyword_score * 0.15                요청 내용에 들어간 키워드가 특정 부서와 얼마나 잘 맞는지
// + document_similarity_score * 0.35    요청 내용이 기존 매뉴얼/워키 문서와 얼마나 비슷한지
// + category_mapping_score * 0.25       사용자가 선택한 카테고리가 어느 부서와 매핑되어 있는지
// + past_ticket_score * 0.20            과거 비슷한 티켓들이 어느 부서에서 처리됐는지
// + llm_classification_score * 0.05     LLM이 문장을 읽고 어떤 부서가 맞다고 분류했는지