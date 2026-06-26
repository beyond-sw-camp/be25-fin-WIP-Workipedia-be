package com.wip.workipedia.admin.esg.dto;

import java.math.BigDecimal;

public record TotalCarbonComparisonDto(
    BigDecimal currentEstimatedCarbonGPerHour,
    BigDecimal recommendedEstimatedCarbonGPerHour,
    BigDecimal estimatedCarbonSavingGPerHour,
    BigDecimal estimatedCarbonSavingGPerDay,
    BigDecimal estimatedCarbonSavingKgPerMonth,
    // 각 리소스의 실제 가동 시작 시각부터 computedAt까지 (배출률 × 가동시간) 합산한 실시간 누적 kg.
    // FE는 이 값에 시간당 비율을 곱해 이어서 라이브 틱한다.
    BigDecimal currentEstimatedCarbonAccumKg,
    BigDecimal recommendedEstimatedCarbonAccumKg,
    BigDecimal estimatedCarbonSavingAccumKg
) {
}
