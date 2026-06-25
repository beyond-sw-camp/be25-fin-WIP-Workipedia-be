package com.wip.workipedia.admin.esg.dto;

import java.math.BigDecimal;

public record InfraSummaryDto(
    int targetResourceCount,
    int recommendedResourceCount,
    String recommendedAction,
    BigDecimal totalEstimatedCarbonSavingGPerHour
) {
}
