package com.wip.workipedia.admin.esg.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.wip.workipedia.config.InfraEsgProperties;
import com.wip.workipedia.admin.esg.domain.OptimizationType;
import com.wip.workipedia.admin.esg.domain.RecommendationStatus;
import com.wip.workipedia.admin.esg.dto.ResourceRecommendationDto;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InfraRecommendationServiceTest {

    private InfraRecommendationService service;

    @BeforeEach
    void setUp() {
        InfraEsgProperties props = new InfraEsgProperties(
            "ap-northeast-2",
            new InfraEsgProperties.Carbon(0.478, 1.135, 0.000392, 0.74, 3.5),
            new InfraEsgProperties.Thresholds(20.0, 50.0),
            Map.of(
                "t3.large", new InfraEsgProperties.InstanceSpec(2, 8),
                "t3.medium", new InfraEsgProperties.InstanceSpec(2, 4)
            ),
            Map.of("t3.large", "t3.medium"),
            List.of()
        );
        service = new InfraRecommendationService(props, new CarbonEstimationService(props));
    }

    @Test
    void lowUsageDownsizableResource_isRecommended() {
        InfraEsgProperties.MonitoredResource be =
            new InfraEsgProperties.MonitoredResource("workipedia-be", "i-be", null, "Backend", "t3.large");

        ResourceRecommendationDto dto = service.evaluate(be, new CpuMetrics(8.4, 23.1));

        assertThat(dto.status()).isEqualTo(RecommendationStatus.RECOMMENDED);
        assertThat(dto.optimizationType()).isEqualTo(OptimizationType.INSTANCE_DOWNSIZE);
        assertThat(dto.recommendedConfiguration()).isEqualTo("t3.medium");
        assertThat(dto.estimatedCarbonSavingGPerHour().doubleValue()).isGreaterThan(0.0);
    }

    @Test
    void highCpuResource_isKept() {
        InfraEsgProperties.MonitoredResource be =
            new InfraEsgProperties.MonitoredResource("workipedia-be", "i-be", null, "Backend", "t3.large");

        ResourceRecommendationDto dto = service.evaluate(be, new CpuMetrics(35.0, 70.0));

        assertThat(dto.status()).isEqualTo(RecommendationStatus.KEEP);
        assertThat(dto.estimatedCarbonSavingGPerHour().doubleValue()).isEqualTo(0.0);
    }

    @Test
    void lowUsageButNoDownsizeTarget_isKept() {
        InfraEsgProperties.MonitoredResource qdrant =
            new InfraEsgProperties.MonitoredResource("workipedia-qdrant", "i-q", null, "Vector DB", "t3.medium");

        ResourceRecommendationDto dto = service.evaluate(qdrant, new CpuMetrics(11.2, 28.6));

        assertThat(dto.status()).isEqualTo(RecommendationStatus.KEEP);
    }

    private InfraEsgProperties.MonitoredResource aiAsg() {
        return new InfraEsgProperties.MonitoredResource(
            "workipedia-ai", null, "workipedia-ai-asg", "AI Server", "t3.large");
    }

    @Test
    void asgUnderUtilizedWithMultipleInstances_recommendsScaleIn() {
        ResourceRecommendationDto dto = service.evaluateAsg(aiAsg(), new CpuMetrics(14.1, 48.5), 2);

        assertThat(dto.status()).isEqualTo(RecommendationStatus.RECOMMENDED);
        assertThat(dto.optimizationType()).isEqualTo(OptimizationType.ASG_SCALE_IN);
        assertThat(dto.currentConfiguration()).isEqualTo("t3.large × 2");
        assertThat(dto.recommendedConfiguration()).isEqualTo("t3.large × 1");
        assertThat(dto.estimatedCarbonSavingGPerHour().doubleValue()).isGreaterThan(0.0);
        // current = perInstance × 2, recommended = perInstance × 1 → saving == perInstance carbon
        assertThat(dto.currentEstimatedCarbonGPerHour().doubleValue())
            .isCloseTo(dto.recommendedEstimatedCarbonGPerHour().doubleValue() * 2,
                org.assertj.core.data.Offset.offset(0.05));
    }

    @Test
    void asgLowAverageCpu_recommendsScaleInEvenWhenMaxCpuSpiked() {
        ResourceRecommendationDto dto = service.evaluateAsg(aiAsg(), new CpuMetrics(14.1, 72.0), 2);

        assertThat(dto.status()).isEqualTo(RecommendationStatus.RECOMMENDED);
        assertThat(dto.optimizationType()).isEqualTo(OptimizationType.ASG_SCALE_IN);
        assertThat(dto.currentConfiguration()).isEqualTo("t3.large × 2");
        assertThat(dto.recommendedConfiguration()).isEqualTo("t3.large × 1");
        assertThat(dto.maxCpu()).isEqualTo(72.0);
    }

    @Test
    void asgSingleInstance_cannotScaleIn_isKept() {
        ResourceRecommendationDto dto = service.evaluateAsg(aiAsg(), new CpuMetrics(14.1, 48.5), 1);

        assertThat(dto.status()).isEqualTo(RecommendationStatus.KEEP);
        assertThat(dto.optimizationType()).isEqualTo(OptimizationType.KEEP);
        assertThat(dto.estimatedCarbonSavingGPerHour().doubleValue()).isEqualTo(0.0);
    }

    @Test
    void asgHighCpu_isKept() {
        ResourceRecommendationDto dto = service.evaluateAsg(aiAsg(), new CpuMetrics(35.0, 70.0), 2);

        assertThat(dto.status()).isEqualTo(RecommendationStatus.KEEP);
        assertThat(dto.estimatedCarbonSavingGPerHour().doubleValue()).isEqualTo(0.0);
    }
}
