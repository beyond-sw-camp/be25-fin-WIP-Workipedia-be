package com.wip.workipedia.storage.service;

import com.wip.workipedia.storage.dto.PresignedDownloadResponse;
import com.wip.workipedia.storage.dto.PresignedUploadRequest;
import com.wip.workipedia.storage.dto.PresignedUploadResponse;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class StorageService {

	private static final Duration UPLOAD_URL_TTL = Duration.ofMinutes(15);
	private static final Duration DOWNLOAD_URL_TTL = Duration.ofHours(1);

	private final S3Client s3Client;
	private final S3Presigner s3Presigner;

	@Value("${r2.bucket}")
	private String bucket;

	@Value("${r2.public-url}")
	private String publicBaseUrl;

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

	public void deleteObject(String objectKey) {
		s3Client.deleteObject(DeleteObjectRequest.builder()
			.bucket(bucket)
			.key(objectKey)
			.build());
	}
}
