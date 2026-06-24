package com.wip.workipedia.admin.esg.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Datapoint;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsResponse;
import software.amazon.awssdk.services.cloudwatch.model.Statistic;

@Service
public class CloudWatchMetricService {

    private static final String NAMESPACE = "AWS/EC2";
    private static final String METRIC_NAME = "CPUUtilization";
    private static final int PERIOD_SECONDS = 86_400;

    private final CloudWatchClient cloudWatchClient;

    public CloudWatchMetricService(CloudWatchClient cloudWatchClient) {
        this.cloudWatchClient = cloudWatchClient;
    }

    public CpuMetrics fetchCpu24h(String instanceId) {
        Instant end = Instant.now();
        Instant start = end.minus(Duration.ofHours(24));

        GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
            .namespace(NAMESPACE)
            .metricName(METRIC_NAME)
            .dimensions(Dimension.builder().name("InstanceId").value(instanceId).build())
            .startTime(start)
            .endTime(end)
            .period(PERIOD_SECONDS)
            .statistics(Statistic.AVERAGE, Statistic.MAXIMUM)
            .build();

        GetMetricStatisticsResponse response = cloudWatchClient.getMetricStatistics(request);
        List<Datapoint> datapoints = response.datapoints();
        if (datapoints.isEmpty()) {
            return new CpuMetrics(0.0, 0.0);
        }

        double avg = datapoints.stream()
            .mapToDouble(dp -> dp.average() == null ? 0.0 : dp.average())
            .average()
            .orElse(0.0);
        double max = datapoints.stream()
            .mapToDouble(dp -> dp.maximum() == null ? 0.0 : dp.maximum())
            .max()
            .orElse(0.0);

        return new CpuMetrics(avg, max);
    }
}
