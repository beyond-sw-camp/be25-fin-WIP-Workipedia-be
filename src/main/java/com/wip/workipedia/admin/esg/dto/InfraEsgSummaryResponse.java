package com.wip.workipedia.admin.esg.dto;

import java.util.List;

public record InfraEsgSummaryResponse(
    String period,
    InfraSummaryDto summary,
    List<ResourceRecommendationDto> resources,
    TotalCarbonComparisonDto totalCarbonComparison,
    EquivalentDto equivalent,
    CalculationDto calculation
) {
}
