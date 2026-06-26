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
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;
import software.amazon.awssdk.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import software.amazon.awssdk.services.autoscaling.model.DescribeAutoScalingGroupsResponse;
import software.amazon.awssdk.services.autoscaling.model.Instance;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Datapoint;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsResponse;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesRequest;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesResponse;

@ExtendWith(MockitoExtension.class)
class CloudWatchMetricServiceTest {

    @Mock CloudWatchClient cloudWatchClient;
    @Mock AutoScalingClient autoScalingClient;
    @Mock Ec2Client ec2Client;
    @Mock RdsClient rdsClient;

    private CloudWatchMetricService service() {
        return new CloudWatchMetricService(cloudWatchClient, autoScalingClient, ec2Client, rdsClient);
    }

    @Test
    void fetchCpu24h_returnsAverageAndMax() {
        GetMetricStatisticsResponse response = GetMetricStatisticsResponse.builder()
            .datapoints(
                Datapoint.builder().timestamp(Instant.now()).average(8.4).maximum(23.1).build())
            .build();
        when(cloudWatchClient.getMetricStatistics(any(GetMetricStatisticsRequest.class)))
            .thenReturn(response);

        CpuMetrics metrics = service().fetchCpu24h("i-be");

        assertThat(metrics.averageCpu()).isEqualTo(8.4);
        assertThat(metrics.maxCpu()).isEqualTo(23.1);
    }

    @Test
    void fetchCpu24h_noDatapoints_returnsZeros() {
        when(cloudWatchClient.getMetricStatistics(any(GetMetricStatisticsRequest.class)))
            .thenReturn(GetMetricStatisticsResponse.builder().build());

        CpuMetrics metrics = service().fetchCpu24h("i-be");

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

        CpuMetrics metrics = service().fetchAsgCpu24h("workipedia-ai-asg");

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
    void fetchRdsCpu24h_queriesRdsNamespaceByDbInstanceIdentifier() {
        GetMetricStatisticsResponse response = GetMetricStatisticsResponse.builder()
            .datapoints(
                Datapoint.builder().timestamp(Instant.now()).average(3.2).maximum(9.8).build())
            .build();
        when(cloudWatchClient.getMetricStatistics(any(GetMetricStatisticsRequest.class)))
            .thenReturn(response);

        CpuMetrics metrics = service().fetchRdsCpu24h("workipedia-db");

        assertThat(metrics.averageCpu()).isEqualTo(3.2);
        assertThat(metrics.maxCpu()).isEqualTo(9.8);

        ArgumentCaptor<GetMetricStatisticsRequest> captor =
            ArgumentCaptor.forClass(GetMetricStatisticsRequest.class);
        org.mockito.Mockito.verify(cloudWatchClient).getMetricStatistics(captor.capture());
        GetMetricStatisticsRequest req = captor.getValue();
        assertThat(req.namespace()).isEqualTo("AWS/RDS");
        assertThat(req.dimensions()).containsExactly(
            Dimension.builder().name("DBInstanceIdentifier").value("workipedia-db").build());
    }

    @Test
    void fetchAsgInServiceCount_countsInServiceInstancesFromAutoScalingApi() {
        DescribeAutoScalingGroupsResponse response = DescribeAutoScalingGroupsResponse.builder()
            .autoScalingGroups(AutoScalingGroup.builder()
                .instances(
                    Instance.builder().lifecycleState("InService").build(),
                    Instance.builder().lifecycleState("InService").build(),
                    Instance.builder().lifecycleState("Terminating").build())
                .build())
            .build();
        when(autoScalingClient.describeAutoScalingGroups(any(DescribeAutoScalingGroupsRequest.class)))
            .thenReturn(response);

        int count = service().fetchAsgInServiceCount("workipedia-ai-asg");

        assertThat(count).isEqualTo(2);

        ArgumentCaptor<DescribeAutoScalingGroupsRequest> captor =
            ArgumentCaptor.forClass(DescribeAutoScalingGroupsRequest.class);
        org.mockito.Mockito.verify(autoScalingClient).describeAutoScalingGroups(captor.capture());
        DescribeAutoScalingGroupsRequest req = captor.getValue();
        assertThat(req.autoScalingGroupNames()).containsExactly("workipedia-ai-asg");
    }

    @Test
    void fetchAsgInServiceCount_noAsg_returnsZero() {
        when(autoScalingClient.describeAutoScalingGroups(any(DescribeAutoScalingGroupsRequest.class)))
            .thenReturn(DescribeAutoScalingGroupsResponse.builder().build());

        int count = service().fetchAsgInServiceCount("workipedia-ai-asg");

        assertThat(count).isEqualTo(0);
    }

    @Test
    void fetchEc2LaunchTime_returnsInstanceLaunchTime() {
        Instant launched = Instant.parse("2026-06-01T00:00:00Z");
        DescribeInstancesResponse response = DescribeInstancesResponse.builder()
            .reservations(Reservation.builder()
                .instances(software.amazon.awssdk.services.ec2.model.Instance.builder()
                    .launchTime(launched).build())
                .build())
            .build();
        when(ec2Client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(response);

        assertThat(service().fetchEc2LaunchTime("i-be")).isEqualTo(launched);
    }

    @Test
    void fetchEc2LaunchTime_noInstance_returnsNull() {
        when(ec2Client.describeInstances(any(DescribeInstancesRequest.class)))
            .thenReturn(DescribeInstancesResponse.builder().build());

        assertThat(service().fetchEc2LaunchTime("i-be")).isNull();
    }

    @Test
    void fetchAsgCreatedTime_returnsGroupCreatedTime() {
        Instant created = Instant.parse("2026-05-20T00:00:00Z");
        when(autoScalingClient.describeAutoScalingGroups(any(DescribeAutoScalingGroupsRequest.class)))
            .thenReturn(DescribeAutoScalingGroupsResponse.builder()
                .autoScalingGroups(AutoScalingGroup.builder().createdTime(created).build())
                .build());

        assertThat(service().fetchAsgCreatedTime("workipedia-ai-asg")).isEqualTo(created);
    }

    @Test
    void fetchRdsCreatedTime_returnsInstanceCreateTime() {
        Instant created = Instant.parse("2026-04-10T00:00:00Z");
        when(rdsClient.describeDBInstances(any(DescribeDbInstancesRequest.class)))
            .thenReturn(DescribeDbInstancesResponse.builder()
                .dbInstances(DBInstance.builder().instanceCreateTime(created).build())
                .build());

        assertThat(service().fetchRdsCreatedTime("workipedia-db")).isEqualTo(created);
    }
}
