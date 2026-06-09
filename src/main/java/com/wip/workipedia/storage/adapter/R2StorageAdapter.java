package com.wip.workipedia.storage.adapter;

import com.wip.workipedia.config.StorageProperties;
import com.wip.workipedia.storage.dto.PresignedDownloadResponse;
import com.wip.workipedia.storage.dto.PresignedUploadRequest;
import com.wip.workipedia.storage.dto.PresignedUploadResponse;
import com.wip.workipedia.storage.port.StoragePort;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

public class R2StorageAdapter implements StoragePort {

    private static final Duration UPLOAD_URL_TTL = Duration.ofMinutes(15);
    private static final Duration DOWNLOAD_URL_TTL = Duration.ofHours(1);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;
    private final String publicBaseUrl;

    public R2StorageAdapter(StorageProperties props) {
        StorageProperties.R2Properties r2 = props.r2();
        String endpoint = "https://" + r2.accountId() + ".r2.cloudflarestorage.com";
        StaticCredentialsProvider credentials = StaticCredentialsProvider.create(
            AwsBasicCredentials.create(r2.accessKey(), r2.secretKey()));

        this.s3Client = S3Client.builder()
            .credentialsProvider(credentials)
            .endpointOverride(URI.create(endpoint))
            .region(Region.of("auto"))
            .build();

        this.s3Presigner = S3Presigner.builder()
            .credentialsProvider(credentials)
            .endpointOverride(URI.create(endpoint))
            .region(Region.of("auto"))
            .build();

        this.bucket = props.bucket();
        this.publicBaseUrl = props.publicUrl();
    }

    @Override
    public PresignedUploadResponse createPresignedUploadUrl(PresignedUploadRequest request) {
        String objectKey = "tickets/replies/" + UUID.randomUUID() + "/" + request.fileName();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(objectKey)
            .contentType(request.contentType())
            .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(UPLOAD_URL_TTL)
            .putObjectRequest(putObjectRequest)
            .build();

        String uploadUrl = s3Presigner.presignPutObject(presignRequest).url().toString();
        String publicUrl = publicBaseUrl.stripTrailing() + "/" + objectKey;
        return new PresignedUploadResponse(uploadUrl, objectKey, publicUrl);
    }

    @Override
    public PresignedDownloadResponse createPresignedDownloadUrl(String objectKey) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(objectKey)
            .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(DOWNLOAD_URL_TTL)
            .getObjectRequest(getObjectRequest)
            .build();

        String downloadUrl = s3Presigner.presignGetObject(presignRequest).url().toString();
        return new PresignedDownloadResponse(downloadUrl);
    }

    @Override
    public void deleteObject(String objectKey) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(objectKey)
            .build());
    }
}
