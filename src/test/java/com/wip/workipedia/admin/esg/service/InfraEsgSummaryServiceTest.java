package com.wip.workipedia.admin.esg.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.wip.workipedia.config.InfraEsgProperties;
import com.wip.workipedia.admin.esg.domain.OptimizationType;
import com.wip.workipedia.admin.esg.dto.InfraEsgSummaryResponse;
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
            new InfraEsgProperties.MonitoredResource("workipedia-be", "i-be", null, "Backend", "t3.large"),
            new InfraEsgProperties.MonitoredResource("workipedia-qdrant", "i-q", null, "Vector DB", "t3.medium")
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
        assertThat(response.totalCarbonComparison().estimatedCarbonSavingGPerHour().doubleValue())
            .isEqualTo(0.0);
    }

    @Test
    void getSummary_asgUnderUtilized_recommendsScaleIn() {
        InfraEsgProperties props = propsWith(List.of(
            new InfraEsgProperties.MonitoredResource(
                "workipedia-ai", null, "workipedia-ai-asg", "AI Server", "t3.large")
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
}
