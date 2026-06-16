package com.wip.workipedia.ticket.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiRoutingResponse(
	Long assignedDepartmentId,
	String assignedDepartmentName,
	BigDecimal confidenceScore,
	BigDecimal scoreMargin,
	String decision,
	List<String> reasons,
	List<AiCandidateDepartment> candidateDepartments,
	String model,
	String provider
) {
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record AiCandidateDepartment(
		Long departmentId,
		String departmentName,
		BigDecimal confidenceScore
	) {}
}
