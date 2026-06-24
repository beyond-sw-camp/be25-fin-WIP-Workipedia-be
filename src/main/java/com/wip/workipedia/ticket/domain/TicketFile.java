package com.wip.workipedia.ticket.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ticket_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ticket_file_id")
	private Long ticketFileId;

	@Column(nullable = false)
	private Long ticketId;

	@Column(nullable = false, length = 500)
	private String fileKey;

	@Column(nullable = false, length = 1000)
	private String fileUrl;

	@Column(length = 255)
	private String fileName;

	@Column(length = 100)
	private String fileContentType;

	private Long fileSize;

	@Column(nullable = false)
	private Integer sortOrder;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	private LocalDateTime deletedAt;

	@Column(nullable = false, length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
	private String isDeleted = "N";

	public static TicketFile create(
		Long ticketId,
		String fileKey,
		String fileUrl,
		String fileName,
		String fileContentType,
		Long fileSize,
		int sortOrder
	) {
		TicketFile file = new TicketFile();
		file.ticketId = ticketId;
		file.fileKey = fileKey;
		file.fileUrl = fileUrl;
		file.fileName = fileName;
		file.fileContentType = fileContentType;
		file.fileSize = fileSize;
		file.sortOrder = sortOrder;
		file.createdAt = LocalDateTime.now();
		return file;
	}
}
