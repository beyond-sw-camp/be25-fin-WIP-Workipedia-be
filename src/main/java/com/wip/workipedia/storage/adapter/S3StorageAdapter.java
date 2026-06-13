package com.wip.workipedia.storage.adapter;

import com.wip.workipedia.config.StorageProperties;
import com.wip.workipedia.storage.dto.PresignedDownloadResponse;
import com.wip.workipedia.storage.dto.PresignedUploadRequest;
import com.wip.workipedia.storage.dto.PresignedUploadResponse;
import com.wip.workipedia.storage.dto.StoredObject;
import com.wip.workipedia.storage.dto.StoredObjectMetadata;
import com.wip.workipedia.storage.port.StoragePort;
import java.time.Duration;
import java.util.UUID;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

public class S3StorageAdapter implements StoragePort {

    private static final Duration UPLOAD_URL_TTL = Duration.ofMinutes(15);
    private static final Duration DOWNLOAD_URL_TTL = Duration.ofHours(1);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;
    private final String publicBaseUrl;

    public S3StorageAdapter(StorageProperties props) {
        StorageProperties.S3Properties s3 = props.s3();
        StaticCredentialsProvider credentials = StaticCredentialsProvider.create(
            AwsBasicCredentials.create(s3.accessKey(), s3.secretKey()));
        Region region = Region.of(s3.region());

        this.s3Client = S3Client.builder()
            .credentialsProvider(credentials)
            .region(region)
            .build();

        this.s3Presigner = S3Presigner.builder()
            .credentialsProvider(credentials)
            .region(region)
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
        String publicUrl = buildPublicUrl(objectKey);
        return new PresignedUploadResponse(uploadUrl, objectKey, publicUrl);
    }

    @Override
    public StoredObject upload(byte[] content, String keyPrefix, String fileName, String contentType) {
        String objectKey = keyPrefix + "/" + UUID.randomUUID() + "/" + fileName;
        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(objectKey)
            .contentType(contentType)
            .build();

        s3Client.putObject(request, RequestBody.fromBytes(content));
        return new StoredObject(objectKey, buildPublicUrl(objectKey));
    }

    @Override
    public StoredObjectMetadata getObjectMetadata(String objectKey) {
        HeadObjectResponse response = s3Client.headObject(HeadObjectRequest.builder()
            .bucket(bucket)
            .key(objectKey)
            .build());
        return new StoredObjectMetadata(
            objectKey,
            buildPublicUrl(objectKey),
            extractFileName(objectKey),
            response.contentType(),
            response.contentLength()
        );
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

    private String buildPublicUrl(String objectKey) {
        String base = publicBaseUrl.endsWith("/")
            ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
            : publicBaseUrl;
        return base + "/" + objectKey;
    }

    private String extractFileName(String objectKey) {
        int lastSlashIndex = objectKey.lastIndexOf('/');
        return lastSlashIndex >= 0 ? objectKey.substring(lastSlashIndex + 1) : objectKey;
    }
}
