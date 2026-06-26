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
import java.time.Duration;
import java.time.Instant;
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

    /** 리소스 평가 결과 + 실제 가동 시작 시각(누적 계산용). 시작 시각을 모르면 runningSince는 null. */
    private record Evaluated(ResourceRecommendationDto dto, Instant runningSince) {}

    @Cacheable("infra:esgSummary")
    public InfraEsgSummaryResponse getSummary() {
        Instant computedAt = Instant.now();

        List<Evaluated> evaluated = properties.resources().stream()
            .map(this::evaluateResource)
            .toList();
        List<ResourceRecommendationDto> resources = evaluated.stream().map(Evaluated::dto).toList();

        List<ResourceRecommendationDto> recommended = resources.stream()
            .filter(r -> r.status() == RecommendationStatus.RECOMMENDED)
            .toList();

        // CURRENT/RECOMMENDED는 전체 리소스를 합산해 인프라 전체 footprint를 보여준다.
        // KEEP 항목은 현재==권장이라 절감(SAVING)에는 추천 항목만 기여한다.
        BigDecimal totalCurrent = sum(resources, ResourceRecommendationDto::currentEstimatedCarbonGPerHour);
        BigDecimal totalRecommended = sum(resources, ResourceRecommendationDto::recommendedEstimatedCarbonGPerHour);
        BigDecimal saving = scale2(totalCurrent.subtract(totalRecommended));
        BigDecimal savingPerDay = scale2(saving.multiply(BigDecimal.valueOf(24)));
        BigDecimal savingKgPerMonth = scale2(
            saving.multiply(BigDecimal.valueOf(24 * 30)).divide(BigDecimal.valueOf(1000)));

        // 리소스별 (배출률 × 실제 가동시간)을 합산한 실시간 누적 kg. 가동 시작 시각을 모르면 0으로 친다.
        BigDecimal currentAccumKg = accumKg(evaluated, computedAt,
            ResourceRecommendationDto::currentEstimatedCarbonGPerHour);
        BigDecimal recommendedAccumKg = accumKg(evaluated, computedAt,
            ResourceRecommendationDto::recommendedEstimatedCarbonGPerHour);
        BigDecimal savingAccumKg = scale2(currentAccumKg.subtract(recommendedAccumKg));

        String action = recommended.isEmpty() ? "KEEP" : "OPTIMIZE";
        InfraSummaryDto summary = new InfraSummaryDto(
            resources.size(), recommended.size(), action, saving);

        TotalCarbonComparisonDto comparison = new TotalCarbonComparisonDto(
            scale2(totalCurrent), scale2(totalRecommended), saving, savingPerDay, savingKgPerMonth,
            currentAccumKg, recommendedAccumKg, savingAccumKg);

        EquivalentDto equivalent = buildEquivalent(saving, savingAccumKg);

        InfraEsgProperties.Carbon c = properties.carbon();
        CalculationDto calculation = new CalculationDto(
            c.emissionFactorKgPerKwh(), c.pue(), c.memoryEnergyKwhPerGbHour(),
            "ESTIMATED", METHODOLOGY);

        return new InfraEsgSummaryResponse(
            PERIOD, computedAt.toEpochMilli(), summary, resources, comparison, equivalent, calculation);
    }

    private Evaluated evaluateResource(InfraEsgProperties.MonitoredResource resource) {
        if (resource.isAsg()) {
            CpuMetrics metrics = cloudWatchMetricService.fetchAsgCpu24h(resource.asgName());
            int inServiceCount = cloudWatchMetricService.fetchAsgInServiceCount(resource.asgName());
            ResourceRecommendationDto dto = recommendationService.evaluateAsg(resource, metrics, inServiceCount);
            return new Evaluated(dto, cloudWatchMetricService.fetchAsgCreatedTime(resource.asgName()));
        }
        if (resource.isRds()) {
            CpuMetrics metrics = cloudWatchMetricService.fetchRdsCpu24h(resource.dbInstanceId());
            ResourceRecommendationDto dto = recommendationService.evaluateRds(resource, metrics);
            return new Evaluated(dto, cloudWatchMetricService.fetchRdsCreatedTime(resource.dbInstanceId()));
        }
        CpuMetrics metrics = cloudWatchMetricService.fetchCpu24h(resource.instanceId());
        ResourceRecommendationDto dto = recommendationService.evaluate(resource, metrics);
        return new Evaluated(dto, cloudWatchMetricService.fetchEc2LaunchTime(resource.instanceId()));
    }

    /** 리소스별 (시간당 배출률 × 가동시간h)을 합산해 kg으로 환산. 가동 시작 시각이 없으면 그 리소스는 0. */
    private BigDecimal accumKg(List<Evaluated> evaluated, Instant computedAt,
                               Function<ResourceRecommendationDto, BigDecimal> rateGetter) {
        BigDecimal grams = evaluated.stream()
            .map(e -> rateGetter.apply(e.dto()).multiply(BigDecimal.valueOf(uptimeHours(e.runningSince(), computedAt))))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return scale2(grams.divide(BigDecimal.valueOf(1000)));
    }

    private double uptimeHours(Instant runningSince, Instant computedAt) {
        if (runningSince == null || !runningSince.isBefore(computedAt)) {
            return 0.0;
        }
        return Duration.between(runningSince, computedAt).toMillis() / 3_600_000.0;
    }

    private EquivalentDto buildEquivalent(BigDecimal savingGramsPerHour, BigDecimal savingAccumKg) {
        BigDecimal chargeKg = EsgEnvironmentImpactCalculator.smartphoneChargeEmissionKgCo2();
        BigDecimal savingKgPerHour = savingGramsPerHour.divide(BigDecimal.valueOf(1000));
        BigDecimal perHour = chargeKg.signum() == 0
            ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
            : savingKgPerHour.divide(chargeKg, 2, RoundingMode.HALF_UP);
        BigDecimal perDay = scale2(perHour.multiply(BigDecimal.valueOf(24)));
        BigDecimal perMonth = scale2(perHour.multiply(BigDecimal.valueOf(24 * 30)));
        BigDecimal accum = chargeKg.signum() == 0
            ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
            : savingAccumKg.divide(chargeKg, 2, RoundingMode.HALF_UP);
        return new EquivalentDto(perHour, perDay, perMonth, accum);
    }

    private BigDecimal sum(List<ResourceRecommendationDto> list,
                           Function<ResourceRecommendationDto, BigDecimal> getter) {
        return list.stream().map(getter).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal scale2(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
