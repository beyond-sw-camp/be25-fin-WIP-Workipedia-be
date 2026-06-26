package com.wip.workipedia.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage")
public record StorageProperties(
    String provider,
    String bucket,
    R2Properties r2,
    MinioProperties minio,
    S3Properties s3
) {
    // public-url은 provider별로 분리한다. 공용 값을 두면 S3가 R2 URL을 잘못 바라보는 사고가 난다.
    public record R2Properties(
        String accessKey,
        String secretKey,
        String accountId,
        String publicUrl
    ) {}

    public record MinioProperties(
        String accessKey,
        String secretKey,
        String endpoint,
        String publicUrl
    ) {}

    public record S3Properties(
        String accessKey,
        String secretKey,
        String region,
        String bucket,
        String publicUrl
    ) {}
}
