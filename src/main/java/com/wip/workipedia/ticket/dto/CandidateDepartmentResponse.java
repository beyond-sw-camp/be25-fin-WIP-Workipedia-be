package com.wip.workipedia.ticket.dto;

import java.math.BigDecimal;

public record CandidateDepartmentResponse(
	Long departmentId,
	String departmentName,
	BigDecimal confidenceScore
) {
}
