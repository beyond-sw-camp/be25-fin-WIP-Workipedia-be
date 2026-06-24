package com.wip.workipedia.admin.esg.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Datapoint;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsResponse;

@ExtendWith(MockitoExtension.class)
class CloudWatchMetricServiceTest {

    @Mock CloudWatchClient cloudWatchClient;

    @Test
    void fetchCpu24h_returnsAverageAndMax() {
        GetMetricStatisticsResponse response = GetMetricStatisticsResponse.builder()
            .datapoints(
                Datapoint.builder().timestamp(Instant.now()).average(8.4).maximum(23.1).build())
            .build();
        when(cloudWatchClient.getMetricStatistics(any(GetMetricStatisticsRequest.class)))
            .thenReturn(response);

        CloudWatchMetricService service = new CloudWatchMetricService(cloudWatchClient);
        CpuMetrics metrics = service.fetchCpu24h("i-be");

        assertThat(metrics.averageCpu()).isEqualTo(8.4);
        assertThat(metrics.maxCpu()).isEqualTo(23.1);
    }

    @Test
    void fetchCpu24h_noDatapoints_returnsZeros() {
        when(cloudWatchClient.getMetricStatistics(any(GetMetricStatisticsRequest.class)))
            .thenReturn(GetMetricStatisticsResponse.builder().build());

        CloudWatchMetricService service = new CloudWatchMetricService(cloudWatchClient);
        CpuMetrics metrics = service.fetchCpu24h("i-be");

        assertThat(metrics.averageCpu()).isEqualTo(0.0);
        assertThat(metrics.maxCpu()).isEqualTo(0.0);
    }
}
