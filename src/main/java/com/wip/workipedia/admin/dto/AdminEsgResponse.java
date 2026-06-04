package com.wip.workipedia.admin.dto;

import java.util.List;

public record AdminEsgResponse(
	long userCount,
	long totalEsgScore,
	double averageEsgScore,
	long highestEsgScore,
	List<EsgGradeDistributionResponse> gradeDistributions
) {
}
