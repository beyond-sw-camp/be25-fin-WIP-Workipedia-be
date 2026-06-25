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

    private static final String EC2_NAMESPACE = "AWS/EC2";
    private static final String AUTOSCALING_NAMESPACE = "AWS/AutoScaling";
    private static final String CPU_METRIC_NAME = "CPUUtilization";
    private static final String IN_SERVICE_METRIC_NAME = "GroupInServiceInstances";
    private static final String INSTANCE_DIMENSION = "InstanceId";
    private static final String ASG_DIMENSION = "AutoScalingGroupName";
    private static final int PERIOD_SECONDS = 86_400;

    private final CloudWatchClient cloudWatchClient;

    public CloudWatchMetricService(CloudWatchClient cloudWatchClient) {
        this.cloudWatchClient = cloudWatchClient;
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
     * <p>{@code AWS/AutoScaling} 네임스페이스의 {@code GroupInServiceInstances} 메트릭으로,
     * 기존 CloudWatch 조회 권한만으로 읽을 수 있다(추가 autoscaling 권한 불필요).
     * 데이터포인트가 없으면 0을 반환한다.
     */
    public int fetchAsgInServiceCount(String asgName) {
        GetMetricStatisticsResponse response = getMetricStatistics(
            AUTOSCALING_NAMESPACE, IN_SERVICE_METRIC_NAME, ASG_DIMENSION, asgName, Statistic.AVERAGE);
        List<Datapoint> datapoints = response.datapoints();
        if (datapoints.isEmpty()) {
            return 0;
        }
        double avg = datapoints.stream()
            .mapToDouble(dp -> dp.average() == null ? 0.0 : dp.average())
            .average()
            .orElse(0.0);
        return (int) Math.round(avg);
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
