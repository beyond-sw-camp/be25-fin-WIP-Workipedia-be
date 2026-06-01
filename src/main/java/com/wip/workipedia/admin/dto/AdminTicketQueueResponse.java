package com.wip.workipedia.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminTicketQueueResponse(
	long ticketId,
	String title,
	String status,
	Long assignedDepartmentId,
	String assignedDepartmentName,
	Long assigneeId,
	String assigneeNickname,
	BigDecimal routingConfidenceScore,
	String routingDecision,
	LocalDateTime createdAt
) {
}
