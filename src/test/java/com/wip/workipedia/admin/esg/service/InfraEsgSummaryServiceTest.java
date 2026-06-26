package com.wip.workipedia.admin.esg.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.wip.workipedia.config.InfraEsgProperties;
import com.wip.workipedia.admin.esg.domain.OptimizationType;
import com.wip.workipedia.admin.esg.dto.InfraEsgSummaryResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InfraEsgSummaryServiceTest {

    @Mock CloudWatchMetricService cloudWatchMetricService;

    private InfraEsgSummaryService service;

    @BeforeEach
    void setUp() {
        InfraEsgProperties props = propsWith(List.of(
            new InfraEsgProperties.MonitoredResource("workipedia-be", "i-be", null, null, "Backend", "t3.large"),
            new InfraEsgProperties.MonitoredResource("workipedia-qdrant", "i-q", null, null, "Vector DB", "t3.medium")
        ));
        service = newService(props);
    }

    private InfraEsgProperties propsWith(List<InfraEsgProperties.MonitoredResource> resources) {
        return new InfraEsgProperties(
            "ap-northeast-2",
            new InfraEsgProperties.Carbon(0.478, 1.135, 0.000392, 0.74, 3.5),
            new InfraEsgProperties.Thresholds(20.0, 50.0),
            Map.of(
                "t3.large", new InfraEsgProperties.InstanceSpec(2, 8),
                "t3.medium", new InfraEsgProperties.InstanceSpec(2, 4)
            ),
            Map.of("t3.large", "t3.medium"),
            resources
        );
    }

    private InfraEsgSummaryService newService(InfraEsgProperties props) {
        return new InfraEsgSummaryService(props, cloudWatchMetricService,
            new InfraRecommendationService(props, new CarbonEstimationService(props)));
    }

    @Test
    void getSummary_aggregatesOnlyRecommended() {
        when(cloudWatchMetricService.fetchCpu24h(eq("i-be"))).thenReturn(new CpuMetrics(8.4, 23.1));
        when(cloudWatchMetricService.fetchCpu24h(eq("i-q"))).thenReturn(new CpuMetrics(11.2, 28.6));

        InfraEsgSummaryResponse response = service.getSummary();

        assertThat(response.period()).isEqualTo("LAST_24_HOURS");
        assertThat(response.summary().targetResourceCount()).isEqualTo(2);
        assertThat(response.summary().recommendedResourceCount()).isEqualTo(1);
        assertThat(response.summary().recommendedAction()).isEqualTo("OPTIMIZE");
        assertThat(response.totalCarbonComparison().estimatedCarbonSavingGPerHour().doubleValue())
            .isGreaterThan(0.0);
        assertThat(response.calculation().measurementType()).isEqualTo("ESTIMATED");
    }

    @Test
    void getSummary_noRecommended_actionKeep() {
        when(cloudWatchMetricService.fetchCpu24h(eq("i-be"))).thenReturn(new CpuMetrics(35.0, 70.0));
        when(cloudWatchMetricService.fetchCpu24h(eq("i-q"))).thenReturn(new CpuMetrics(11.2, 28.6));

        InfraEsgSummaryResponse response = service.getSummary();

        assertThat(response.summary().recommendedResourceCount()).isEqualTo(0);
        assertThat(response.summary().recommendedAction()).isEqualTo("KEEP");
        // 추천이 0개여도 CURRENT/RECOMMENDED는 전체 리소스의 현재 배출을 보여준다(전체 footprint).
        assertThat(response.totalCarbonComparison().currentEstimatedCarbonGPerHour().doubleValue())
            .isGreaterThan(0.0);
        // 추천이 없으니 권장 배출 == 현재 배출, 절감은 0.
        assertThat(response.totalCarbonComparison().recommendedEstimatedCarbonGPerHour().doubleValue())
            .isEqualTo(response.totalCarbonComparison().currentEstimatedCarbonGPerHour().doubleValue());
        assertThat(response.totalCarbonComparison().estimatedCarbonSavingGPerHour().doubleValue())
            .isEqualTo(0.0);
    }

    @Test
    void getSummary_asgUnderUtilized_recommendsScaleIn() {
        InfraEsgProperties props = propsWith(List.of(
            new InfraEsgProperties.MonitoredResource(
                "workipedia-ai", null, "workipedia-ai-asg", null, "AI Server", "t3.large")
        ));
        InfraEsgSummaryService asgService = newService(props);

        when(cloudWatchMetricService.fetchAsgCpu24h(eq("workipedia-ai-asg")))
            .thenReturn(new CpuMetrics(14.1, 48.5));
        when(cloudWatchMetricService.fetchAsgInServiceCount(eq("workipedia-ai-asg")))
            .thenReturn(2);

        InfraEsgSummaryResponse response = asgService.getSummary();

        assertThat(response.summary().recommendedResourceCount()).isEqualTo(1);
        assertThat(response.resources().get(0).optimizationType())
            .isEqualTo(OptimizationType.ASG_SCALE_IN);
        assertThat(response.resources().get(0).currentConfiguration()).isEqualTo("t3.large × 2");
        assertThat(response.totalCarbonComparison().estimatedCarbonSavingGPerHour().doubleValue())
            .isGreaterThan(0.0);
    }

    @Test
    void getSummary_accumulatesFromResourceLaunchTime() {
        when(cloudWatchMetricService.fetchCpu24h(eq("i-be"))).thenReturn(new CpuMetrics(8.4, 23.1));
        when(cloudWatchMetricService.fetchCpu24h(eq("i-q"))).thenReturn(new CpuMetrics(11.2, 28.6));
        when(cloudWatchMetricService.fetchEc2LaunchTime(eq("i-be")))
            .thenReturn(Instant.now().minus(Duration.ofHours(10)));
        when(cloudWatchMetricService.fetchEc2LaunchTime(eq("i-q")))
            .thenReturn(Instant.now().minus(Duration.ofHours(10)));

        InfraEsgSummaryResponse response = service.getSummary();

        assertThat(response.computedAtEpochMs()).isGreaterThan(0L);
        // 가동 시작(10시간 전)부터 누적되므로 0보다 크다.
        assertThat(response.totalCarbonComparison().currentEstimatedCarbonAccumKg().doubleValue())
            .isGreaterThan(0.0);
        assertThat(response.totalCarbonComparison().estimatedCarbonSavingAccumKg().doubleValue())
            .isGreaterThan(0.0);
    }

    @Test
    void getSummary_unknownLaunchTime_accumIsZero() {
        when(cloudWatchMetricService.fetchCpu24h(eq("i-be"))).thenReturn(new CpuMetrics(8.4, 23.1));
        when(cloudWatchMetricService.fetchCpu24h(eq("i-q"))).thenReturn(new CpuMetrics(11.2, 28.6));
        // fetchEc2LaunchTime 미스텁 → null → 누적 0

        InfraEsgSummaryResponse response = service.getSummary();

        assertThat(response.totalCarbonComparison().currentEstimatedCarbonAccumKg().doubleValue())
            .isEqualTo(0.0);
        assertThat(response.totalCarbonComparison().estimatedCarbonSavingAccumKg().doubleValue())
            .isEqualTo(0.0);
    }
}
