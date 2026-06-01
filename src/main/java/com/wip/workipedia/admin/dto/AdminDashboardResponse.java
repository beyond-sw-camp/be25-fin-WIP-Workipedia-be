package com.wip.workipedia.admin.dto;

// 관리자 대시보드 상단 카드에 보여줄 운영 지표 응답입니다.
public record AdminDashboardResponse(
	long teamQueueCount,
	long commonQueueCount,
	long completedTicketCount,
	long knowledgeReviewTicketCount, // 지식화 검토 대상 처리 완료 티켓
	long knowledgePublishedCount, // 검수 완료 후 워키 반영
	double autoAssignmentSuccessRate // 자동 배정 성공률
) {
}
