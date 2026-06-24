package com.wip.workipedia.infra.esg.dto;

import com.wip.workipedia.infra.esg.domain.OptimizationType;
import com.wip.workipedia.infra.esg.domain.RecommendationStatus;
import java.math.BigDecimal;

public record ResourceRecommendationDto(
    String resourceName,
    String role,
    OptimizationType optimizationType,
    String currentConfiguration,
    String recommendedConfiguration,
    double averageCpu,
    double maxCpu,
    BigDecimal currentEstimatedCarbonGPerHour,
    BigDecimal recommendedEstimatedCarbonGPerHour,
    BigDecimal estimatedCarbonSavingGPerHour,
    String recommendation,
    RecommendationStatus status
) {
}
