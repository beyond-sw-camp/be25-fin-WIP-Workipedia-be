package com.wip.workipedia.admin.esg.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import software.amazon.awssdk.services.autoscaling.model.DescribeAutoScalingGroupsResponse;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Datapoint;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsResponse;
import software.amazon.awssdk.services.cloudwatch.model.Statistic;

@Service
public class CloudWatchMetricService {

    private static final String EC2_NAMESPACE = "AWS/EC2";
    private static final String CPU_METRIC_NAME = "CPUUtilization";
    private static final String INSTANCE_DIMENSION = "InstanceId";
    private static final String ASG_DIMENSION = "AutoScalingGroupName";
    private static final int PERIOD_SECONDS = 86_400;

    private final CloudWatchClient cloudWatchClient;
    private final AutoScalingClient autoScalingClient;

    public CloudWatchMetricService(CloudWatchClient cloudWatchClient, AutoScalingClient autoScalingClient) {
        this.cloudWatchClient = cloudWatchClient;
        this.autoScalingClient = autoScalingClient;
    }

    /** 단일 EC2 인스턴스의 지난 24시간 CPU(평균/최대)를 조회한다. */
    public CpuMetrics fetchCpu24h(String instanceId) {
        return fetchCpuByDimension(INSTANCE_DIMENSION, instanceId);
    }

    /**
     * Auto Scaling Group 전체의 지난 24시간 CPU(평균/최대)를 조회한다.
     *
     * <p>{@code AWS/EC2} 네임스페이스를 {@code AutoScalingGroupName} 차원으로 조회하면
     * CloudWatch가 그룹에 속한 인스턴스들의 CPU를 집계해 돌려준다. 인스턴스 ID가
     * 스케일 이벤트로 바뀌어도 ASG 이름만으로 안정적으로 집계할 수 있다.
     */
    public CpuMetrics fetchAsgCpu24h(String asgName) {
        return fetchCpuByDimension(ASG_DIMENSION, asgName);
    }

    /**
     * Auto Scaling Group의 현재 InService 인스턴스 수를 조회한다.
     *
     * <p>인스턴스 수는 CloudWatch 메트릭 지연에 영향을 받지 않도록 Auto Scaling API에서
     * 직접 조회한다. CloudWatch {@code GroupInServiceInstances}는 ASG 생성 직후나
     * 메트릭 수집 전에는 0처럼 보일 수 있다.
     */
    public int fetchAsgInServiceCount(String asgName) {
        DescribeAutoScalingGroupsResponse response = autoScalingClient.describeAutoScalingGroups(
            DescribeAutoScalingGroupsRequest.builder()
                .autoScalingGroupNames(asgName)
                .build());

        return response.autoScalingGroups().stream()
            .findFirst()
            .map(group -> (int) group.instances().stream()
                .filter(instance -> "InService".equals(instance.lifecycleStateAsString()))
                .count())
            .orElse(0);
    }

    private CpuMetrics fetchCpuByDimension(String dimensionName, String dimensionValue) {
        GetMetricStatisticsResponse response = getMetricStatistics(
            EC2_NAMESPACE, CPU_METRIC_NAME, dimensionName, dimensionValue,
            Statistic.AVERAGE, Statistic.MAXIMUM);
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

    private GetMetricStatisticsResponse getMetricStatistics(
        String namespace, String metricName, String dimensionName, String dimensionValue,
        Statistic... statistics) {
        Instant end = Instant.now();
        Instant start = end.minus(Duration.ofHours(24));

        GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
            .namespace(namespace)
            .metricName(metricName)
            .dimensions(Dimension.builder().name(dimensionName).value(dimensionValue).build())
            .startTime(start)
            .endTime(end)
            .period(PERIOD_SECONDS)
            .statistics(statistics)
            .build();

        return cloudWatchClient.getMetricStatistics(request);
    }
}
