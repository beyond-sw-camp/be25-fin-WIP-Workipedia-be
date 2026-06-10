package com.wip.workipedia.storage.dto;

public record PresignedUploadResponse(
	String uploadUrl,
	String objectKey,
	String publicUrl
) {
}
