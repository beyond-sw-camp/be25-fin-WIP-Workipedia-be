package com.wip.workipedia.storage.dto;

import jakarta.validation.constraints.NotBlank;

public record PresignedUploadRequest(
	@NotBlank String fileName,
	@NotBlank String contentType
) {
}
