package com.wip.workipedia.admin.esg.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;
import software.amazon.awssdk.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import software.amazon.awssdk.services.autoscaling.model.DescribeAutoScalingGroupsResponse;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Datapoint;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsResponse;
import software.amazon.awssdk.services.cloudwatch.model.Statistic;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesRequest;

@Service
public class CloudWatchMetricService {

    private static final String EC2_NAMESPACE = "AWS/EC2";
    private static final String RDS_NAMESPACE = "AWS/RDS";
    private static final String CPU_METRIC_NAME = "CPUUtilization";
    private static final String INSTANCE_DIMENSION = "InstanceId";
    private static final String ASG_DIMENSION = "AutoScalingGroupName";
    private static final String RDS_DIMENSION = "DBInstanceIdentifier";
    private static final int PERIOD_SECONDS = 86_400;

    private static final Logger log = LoggerFactory.getLogger(CloudWatchMetricService.class);

    private final CloudWatchClient cloudWatchClient;
    private final AutoScalingClient autoScalingClient;
    private final Ec2Client ec2Client;
    private final RdsClient rdsClient;

    public CloudWatchMetricService(CloudWatchClient cloudWatchClient,
                                   AutoScalingClient autoScalingClient,
                                   Ec2Client ec2Client,
                                   RdsClient rdsClient) {
        this.cloudWatchClient = cloudWatchClient;
        this.autoScalingClient = autoScalingClient;
        this.ec2Client = ec2Client;
        this.rdsClient = rdsClient;
    }

    /** 단일 EC2 인스턴스의 지난 24시간 CPU(평균/최대)를 조회한다. */
    public CpuMetrics fetchCpu24h(String instanceId) {
        return fetchCpuByDimension(EC2_NAMESPACE, INSTANCE_DIMENSION, instanceId);
    }

    /** RDS 인스턴스의 지난 24시간 CPU(평균/최대)를 조회한다. (AWS/RDS 네임스페이스) */
    public CpuMetrics fetchRdsCpu24h(String dbInstanceId) {
        return fetchCpuByDimension(RDS_NAMESPACE, RDS_DIMENSION, dbInstanceId);
    }

    // 가동 시작 시각 조회는 식별자가 틀리거나(존재하지 않는 인스턴스) 권한·네트워크 문제로 예외를 던질 수 있다.
    // 이 값은 "실시간 누적"의 부가 정보일 뿐이라, 실패해도 대시보드 전체가 깨지지 않게 null로 떨어뜨린다(누적 0).

    /** EC2 인스턴스의 시작 시각(launchTime)을 조회한다. 조회 실패/없음 시 null. */
    public Instant fetchEc2LaunchTime(String instanceId) {
        try {
            return ec2Client.describeInstances(
                    DescribeInstancesRequest.builder().instanceIds(instanceId).build())
                .reservations().stream()
                .flatMap(r -> r.instances().stream())
                .findFirst()
                .map(software.amazon.awssdk.services.ec2.model.Instance::launchTime)
                .orElse(null);
        } catch (RuntimeException e) {
            log.warn("EC2 launchTime 조회 실패. instanceId={}, error={}", instanceId, e.getMessage());
            return null;
        }
    }

    /** Auto Scaling Group의 생성 시각(createdTime)을 조회한다. 인스턴스 교체와 무관하게 안정적. 조회 실패/없음 시 null. */
    public Instant fetchAsgCreatedTime(String asgName) {
        try {
            return autoScalingClient.describeAutoScalingGroups(
                    DescribeAutoScalingGroupsRequest.builder().autoScalingGroupNames(asgName).build())
                .autoScalingGroups().stream()
                .findFirst()
                .map(AutoScalingGroup::createdTime)
                .orElse(null);
        } catch (RuntimeException e) {
            log.warn("ASG createdTime 조회 실패. asgName={}, error={}", asgName, e.getMessage());
            return null;
        }
    }

    /** RDS 인스턴스의 생성 시각(instanceCreateTime)을 조회한다. 조회 실패/없음 시 null. */
    public Instant fetchRdsCreatedTime(String dbInstanceId) {
        try {
            return rdsClient.describeDBInstances(
                    DescribeDbInstancesRequest.builder().dbInstanceIdentifier(dbInstanceId).build())
                .dbInstances().stream()
                .findFirst()
                .map(DBInstance::instanceCreateTime)
                .orElse(null);
        } catch (RuntimeException e) {
            log.warn("RDS instanceCreateTime 조회 실패. dbInstanceId={}, error={}", dbInstanceId, e.getMessage());
            return null;
        }
    }

    /**
     * Auto Scaling Group 전체의 지난 24시간 CPU(평균/최대)를 조회한다.
     *
     * <p>{@code AWS/EC2} 네임스페이스를 {@code AutoScalingGroupName} 차원으로 조회하면
     * CloudWatch가 그룹에 속한 인스턴스들의 CPU를 집계해 돌려준다. 인스턴스 ID가
     * 스케일 이벤트로 바뀌어도 ASG 이름만으로 안정적으로 집계할 수 있다.
     */
    public CpuMetrics fetchAsgCpu24h(String asgName) {
        return fetchCpuByDimension(EC2_NAMESPACE, ASG_DIMENSION, asgName);
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

    private CpuMetrics fetchCpuByDimension(String namespace, String dimensionName, String dimensionValue) {
        GetMetricStatisticsResponse response = getMetricStatistics(
            namespace, CPU_METRIC_NAME, dimensionName, dimensionValue,
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
