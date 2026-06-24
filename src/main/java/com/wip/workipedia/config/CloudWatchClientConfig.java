package com.wip.workipedia.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

@Configuration
@EnableConfigurationProperties(InfraEsgProperties.class)
public class CloudWatchClientConfig {

    @Bean
    public CloudWatchClient cloudWatchClient(
        InfraEsgProperties properties,
        @Value("${aws.credentials.access-key}") String accessKey,
        @Value("${aws.credentials.secret-key}") String secretKey
    ) {
        StaticCredentialsProvider credentials = StaticCredentialsProvider.create(
            AwsBasicCredentials.create(accessKey, secretKey));
        return CloudWatchClient.builder()
            .credentialsProvider(credentials)
            .region(Region.of(properties.region()))
            .build();
    }
}
