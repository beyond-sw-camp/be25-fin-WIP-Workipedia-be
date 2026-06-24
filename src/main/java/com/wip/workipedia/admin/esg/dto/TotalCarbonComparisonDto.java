package com.wip.workipedia.admin.esg.dto;

import java.math.BigDecimal;

public record TotalCarbonComparisonDto(
    BigDecimal currentEstimatedCarbonGPerHour,
    BigDecimal recommendedEstimatedCarbonGPerHour,
    BigDecimal estimatedCarbonSavingGPerHour,
    BigDecimal estimatedCarbonSavingGPerDay,
    BigDecimal estimatedCarbonSavingKgPerMonth
) {
}
