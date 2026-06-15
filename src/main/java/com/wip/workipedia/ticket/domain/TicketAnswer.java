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
@Table(name = "ticket_answers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketAnswer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ticket_answer_id")
	private Long ticketAnswerId;

	@Column(nullable = false)
	private Long ticketId;

	@Column(nullable = false)
	private Long authorId;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(length = 500)
	private String fileKey;

	@Column(length = 1000)
	private String fileUrl;

	@Column(length = 255)
	private String fileName;

	@Column(length = 100)
	private String fileContentType;

	private Long fileSize;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	private LocalDateTime deletedAt;

	@Column(nullable = false, length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
	private String isDeleted = "N";

	public static TicketAnswer create(
		Long ticketId,
		Long authorId,
		String content,
		String fileKey,
		String fileUrl,
		String fileName,
		String fileContentType,
		Long fileSize
	) {
		TicketAnswer answer = new TicketAnswer();
		answer.ticketId = ticketId;
		answer.authorId = authorId;
		answer.content = content;
		answer.fileKey = fileKey;
		answer.fileUrl = fileUrl;
		answer.fileName = fileName;
		answer.fileContentType = fileContentType;
		answer.fileSize = fileSize;
		answer.createdAt = LocalDateTime.now();
		return answer;
	}
}
