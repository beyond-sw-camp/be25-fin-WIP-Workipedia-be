package com.wip.workipedia.admin.esg.dto;

public record CalculationDto(
    double emissionFactorKgPerKwh,
    double awsPue,
    double memoryEnergyKwhPerGbHour,
    String measurementType,
    String methodology
) {
}
