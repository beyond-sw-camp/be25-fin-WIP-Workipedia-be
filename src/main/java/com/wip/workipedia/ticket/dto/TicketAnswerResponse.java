package com.wip.workipedia.ticket.dto;

import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.ticket.domain.TicketAnswer;
import com.wip.workipedia.user.domain.User;
import java.time.LocalDateTime;

public record TicketAnswerResponse(
	Long answerId,
	Long ticketId,
	String content,
	Long authorId,
	String authorNickname,
	Long authorDepartmentId,
	String authorDepartmentName,
	String fileKey,
	String fileUrl,
	String fileName,
	String fileContentType,
	Long fileSize,
	LocalDateTime answeredAt
) {

	public static TicketAnswerResponse from(TicketAnswer answer, User author) {
		Department department = author == null ? null : author.getDepartment();
		return new TicketAnswerResponse(
			answer.getTicketAnswerId(),
			answer.getTicketId(),
			answer.getContent(),
			answer.getAuthorId(),
			author == null ? null : author.getNickname(),
			department == null ? null : department.getDepartmentId(),
			department == null ? null : department.getDepartmentName(),
			answer.getFileKey(),
			answer.getFileUrl(),
			answer.getFileName(),
			answer.getFileContentType(),
			answer.getFileSize(),
			answer.getCreatedAt()
		);
	}
}
