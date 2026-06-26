package com.wip.workipedia.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

@Configuration
@EnableConfigurationProperties(InfraEsgProperties.class)
public class CloudWatchClientConfig {

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider(
        @Value("${aws.credentials.access-key}") String accessKey,
        @Value("${aws.credentials.secret-key}") String secretKey
    ) {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
    }

    @Bean
    public CloudWatchClient cloudWatchClient(
        InfraEsgProperties properties,
        AwsCredentialsProvider awsCredentialsProvider
    ) {
        return CloudWatchClient.builder()
            .credentialsProvider(awsCredentialsProvider)
            .region(Region.of(properties.region()))
            .build();
    }

    @Bean
    public AutoScalingClient autoScalingClient(
        InfraEsgProperties properties,
        AwsCredentialsProvider awsCredentialsProvider
    ) {
        return AutoScalingClient.builder()
            .credentialsProvider(awsCredentialsProvider)
            .region(Region.of(properties.region()))
            .build();
    }
}
