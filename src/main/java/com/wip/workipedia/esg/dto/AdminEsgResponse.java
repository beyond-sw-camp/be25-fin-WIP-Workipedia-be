package com.wip.workipedia.esg.dto;

import java.util.List;

public record AdminEsgResponse(
	long userCount,
	long totalEsgScore,
	double averageEsgScore,
	long highestEsgScore,
	List<EsgGradeDistributionResponse> gradeDistributions
) {
}
