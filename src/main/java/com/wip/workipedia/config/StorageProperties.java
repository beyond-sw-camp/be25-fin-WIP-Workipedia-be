package com.wip.workipedia.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage")
public record StorageProperties(
    String provider,
    String bucket,
    String publicUrl,
    R2Properties r2,
    MinioProperties minio,
    S3Properties s3
) {
    public record R2Properties(
        String accessKey,
        String secretKey,
        String accountId
    ) {}

    public record MinioProperties(
        String accessKey,
        String secretKey,
        String endpoint
    ) {}

    public record S3Properties(
        String accessKey,
        String secretKey,
        String region
    ) {}
}
