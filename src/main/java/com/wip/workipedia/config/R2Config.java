package com.wip.workipedia.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class R2Config {

	@Value("${r2.access-key}")
	private String accessKey;

	@Value("${r2.secret-key}")
	private String secretKey;

	@Value("${r2.account-id}")
	private String accountId;

	@Bean
	public S3Client s3Client() {
		return S3Client.builder()
			.credentialsProvider(StaticCredentialsProvider.create(
				AwsBasicCredentials.create(accessKey, secretKey)))
			.endpointOverride(URI.create("https://" + accountId + ".r2.cloudflarestorage.com"))
			.region(Region.of("auto"))
			.build();
	}

	@Bean
	public S3Presigner s3Presigner() {
		return S3Presigner.builder()
			.credentialsProvider(StaticCredentialsProvider.create(
				AwsBasicCredentials.create(accessKey, secretKey)))
			.endpointOverride(URI.create("https://" + accountId + ".r2.cloudflarestorage.com"))
			.region(Region.of("auto"))
			.build();
	}
}
