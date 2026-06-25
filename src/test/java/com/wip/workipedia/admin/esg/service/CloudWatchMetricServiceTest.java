package com.wip.workipedia.admin.esg.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Datapoint;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
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

    @Test
    void fetchAsgCpu24h_queriesByAutoScalingGroupNameDimension() {
        GetMetricStatisticsResponse response = GetMetricStatisticsResponse.builder()
            .datapoints(
                Datapoint.builder().timestamp(Instant.now()).average(14.1).maximum(48.5).build())
            .build();
        when(cloudWatchClient.getMetricStatistics(any(GetMetricStatisticsRequest.class)))
            .thenReturn(response);

        CloudWatchMetricService service = new CloudWatchMetricService(cloudWatchClient);
        CpuMetrics metrics = service.fetchAsgCpu24h("workipedia-ai-asg");

        assertThat(metrics.averageCpu()).isEqualTo(14.1);
        assertThat(metrics.maxCpu()).isEqualTo(48.5);

        ArgumentCaptor<GetMetricStatisticsRequest> captor =
            ArgumentCaptor.forClass(GetMetricStatisticsRequest.class);
        org.mockito.Mockito.verify(cloudWatchClient).getMetricStatistics(captor.capture());
        GetMetricStatisticsRequest req = captor.getValue();
        assertThat(req.namespace()).isEqualTo("AWS/EC2");
        assertThat(req.metricName()).isEqualTo("CPUUtilization");
        assertThat(req.dimensions()).containsExactly(
            Dimension.builder().name("AutoScalingGroupName").value("workipedia-ai-asg").build());
    }

    @Test
    void fetchAsgInServiceCount_returnsRoundedCountFromAutoScalingNamespace() {
        GetMetricStatisticsResponse response = GetMetricStatisticsResponse.builder()
            .datapoints(
                Datapoint.builder().timestamp(Instant.now()).average(2.0).build())
            .build();
        when(cloudWatchClient.getMetricStatistics(any(GetMetricStatisticsRequest.class)))
            .thenReturn(response);

        CloudWatchMetricService service = new CloudWatchMetricService(cloudWatchClient);
        int count = service.fetchAsgInServiceCount("workipedia-ai-asg");

        assertThat(count).isEqualTo(2);

        ArgumentCaptor<GetMetricStatisticsRequest> captor =
            ArgumentCaptor.forClass(GetMetricStatisticsRequest.class);
        org.mockito.Mockito.verify(cloudWatchClient).getMetricStatistics(captor.capture());
        GetMetricStatisticsRequest req = captor.getValue();
        assertThat(req.namespace()).isEqualTo("AWS/AutoScaling");
        assertThat(req.metricName()).isEqualTo("GroupInServiceInstances");
        assertThat(req.dimensions()).containsExactly(
            Dimension.builder().name("AutoScalingGroupName").value("workipedia-ai-asg").build());
    }

    @Test
    void fetchAsgInServiceCount_noDatapoints_returnsZero() {
        when(cloudWatchClient.getMetricStatistics(any(GetMetricStatisticsRequest.class)))
            .thenReturn(GetMetricStatisticsResponse.builder().build());

        CloudWatchMetricService service = new CloudWatchMetricService(cloudWatchClient);
        int count = service.fetchAsgInServiceCount("workipedia-ai-asg");

        assertThat(count).isEqualTo(0);
    }
}
