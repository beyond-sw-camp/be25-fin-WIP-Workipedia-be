package com.wip.workipedia.ticket.dto;

import com.wip.workipedia.ticket.domain.RoutingDecision;
import java.math.BigDecimal;
import java.util.List;

public record RoutingResult(
	Long assignedDepartmentId,
	String assignedDepartmentName,
	BigDecimal confidenceScore,
	BigDecimal scoreMargin,
	String modelVersion,
	RoutingDecision decision,
	List<String> reasons,
	List<CandidateDepartmentResponse> candidateDepartments
) {
}
