package com.wip.workipedia.ticket.dto;

import com.wip.workipedia.ticket.domain.TicketFile;

public record TicketFileResponse(
	Long fileId,
	String fileKey,
	String fileUrl,
	String fileName,
	String fileContentType,
	Long fileSize
) {
	public static TicketFileResponse from(TicketFile file) {
		return new TicketFileResponse(
			file.getTicketFileId(),
			file.getFileKey(),
			file.getFileUrl(),
			file.getFileName(),
			file.getFileContentType(),
			file.getFileSize()
		);
	}
}
