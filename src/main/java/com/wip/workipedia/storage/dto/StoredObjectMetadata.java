package com.wip.workipedia.storage.dto;

public record StoredObjectMetadata(
	String objectKey,
	String publicUrl,
	String fileName,
	String contentType,
	Long contentLength
) {
}
