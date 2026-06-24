package com.wip.workipedia.admin.esg.service;

import com.wip.workipedia.config.InfraEsgProperties;
import com.wip.workipedia.admin.esg.domain.RecommendationStatus;
import com.wip.workipedia.admin.esg.dto.CalculationDto;
import com.wip.workipedia.admin.esg.dto.EquivalentDto;
import com.wip.workipedia.admin.esg.dto.InfraEsgSummaryResponse;
import com.wip.workipedia.admin.esg.dto.InfraSummaryDto;
import com.wip.workipedia.admin.esg.dto.ResourceRecommendationDto;
import com.wip.workipedia.admin.esg.dto.TotalCarbonComparisonDto;
import com.wip.workipedia.leaderboard.service.EsgEnvironmentImpactCalculator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Function;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class InfraEsgSummaryService {

    private static final String PERIOD = "LAST_24_HOURS";
    private static final String METHODOLOGY =
        "CloudWatch metrics + AWS resource metadata + Cloud Carbon Footprint public coefficients "
            + "+ Korea electricity emission factor";

    private final InfraEsgProperties properties;
    private final CloudWatchMetricService cloudWatchMetricService;
    private final InfraRecommendationService recommendationService;

    public InfraEsgSummaryService(InfraEsgProperties properties,
                                  CloudWatchMetricService cloudWatchMetricService,
                                  InfraRecommendationService recommendationService) {
        this.properties = properties;
        this.cloudWatchMetricService = cloudWatchMetricService;
        this.recommendationService = recommendationService;
    }

    @Cacheable("infra:esgSummary")
    public InfraEsgSummaryResponse getSummary() {
        List<ResourceRecommendationDto> resources = properties.resources().stream()
            .map(resource -> recommendationService.evaluate(
                resource, cloudWatchMetricService.fetchCpu24h(resource.instanceId())))
            .toList();

        List<ResourceRecommendationDto> recommended = resources.stream()
            .filter(r -> r.status() == RecommendationStatus.RECOMMENDED)
            .toList();

        BigDecimal totalCurrent = sum(recommended, ResourceRecommendationDto::currentEstimatedCarbonGPerHour);
        BigDecimal totalRecommended = sum(recommended, ResourceRecommendationDto::recommendedEstimatedCarbonGPerHour);
        BigDecimal saving = scale2(totalCurrent.subtract(totalRecommended));
        BigDecimal savingPerDay = scale2(saving.multiply(BigDecimal.valueOf(24)));
        BigDecimal savingKgPerMonth = scale2(
            saving.multiply(BigDecimal.valueOf(24 * 30)).divide(BigDecimal.valueOf(1000)));

        String action = recommended.isEmpty() ? "KEEP" : "OPTIMIZE";
        InfraSummaryDto summary = new InfraSummaryDto(
            resources.size(), recommended.size(), action, saving);

        TotalCarbonComparisonDto comparison = new TotalCarbonComparisonDto(
            scale2(totalCurrent), scale2(totalRecommended), saving, savingPerDay, savingKgPerMonth);

        EquivalentDto equivalent = buildEquivalent(saving);

        InfraEsgProperties.Carbon c = properties.carbon();
        CalculationDto calculation = new CalculationDto(
            c.emissionFactorKgPerKwh(), c.pue(), c.memoryEnergyKwhPerGbHour(),
            "ESTIMATED", METHODOLOGY);

        return new InfraEsgSummaryResponse(
            PERIOD, summary, resources, comparison, equivalent, calculation);
    }

    private EquivalentDto buildEquivalent(BigDecimal savingGramsPerHour) {
        BigDecimal chargeKg = EsgEnvironmentImpactCalculator.smartphoneChargeEmissionKgCo2();
        BigDecimal savingKgPerHour = savingGramsPerHour.divide(BigDecimal.valueOf(1000));
        BigDecimal perHour = chargeKg.signum() == 0
            ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
            : savingKgPerHour.divide(chargeKg, 2, RoundingMode.HALF_UP);
        BigDecimal perDay = scale2(perHour.multiply(BigDecimal.valueOf(24)));
        BigDecimal perMonth = scale2(perHour.multiply(BigDecimal.valueOf(24 * 30)));
        return new EquivalentDto(perHour, perDay, perMonth);
    }

    private BigDecimal sum(List<ResourceRecommendationDto> list,
                           Function<ResourceRecommendationDto, BigDecimal> getter) {
        return list.stream().map(getter).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal scale2(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
