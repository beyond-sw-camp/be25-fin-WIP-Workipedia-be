package com.wip.workipedia.admin.esg.dto;

import java.util.List;

public record InfraEsgSummaryResponse(
    String period,
    // 누적값 기준 시각(epoch millis). FE는 (누적 + 시간당비율 × (now − computedAt))로 라이브 틱한다.
    long computedAtEpochMs,
    InfraSummaryDto summary,
    List<ResourceRecommendationDto> resources,
    TotalCarbonComparisonDto totalCarbonComparison,
    EquivalentDto equivalent,
    CalculationDto calculation
) {
}
