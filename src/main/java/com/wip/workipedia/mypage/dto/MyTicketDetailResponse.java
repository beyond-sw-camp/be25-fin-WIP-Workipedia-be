package com.wip.workipedia.mypage.dto;

import com.wip.workipedia.mypage.repository.MyTicketDetailProjection;
import com.wip.workipedia.ticket.dto.TicketFileResponse;
import java.time.LocalDateTime;
import java.util.List;

public record MyTicketDetailResponse(
	Long ticketId,
	String title,
	String content,
	Long assignedDepartmentId,
	String assignedDepartmentName,
	String status,
	Long remainingHours,
	boolean expired,
	boolean editable,
	boolean deletable,
	LocalDateTime assignedAt,
	LocalDateTime createdAt,
	LocalDateTime completedAt,
	String fileUrl,
	List<TicketFileResponse> files,
	Answer answer
) {

	public record Answer(
		Long answerId,
		String content,
		Long authorId,
		String authorNickname,
		Long authorDepartmentId,
		String authorDepartmentName,
		LocalDateTime answeredAt
	) {
	}

	public static MyTicketDetailResponse from(
		MyTicketDetailProjection projection,
		Long remainingHours,
		boolean expired,
		boolean editable,
		boolean deletable,
		List<TicketFileResponse> files
	) {
		return new MyTicketDetailResponse(
			projection.getTicketId(),
			projection.getTitle(),
			projection.getContent(),
			projection.getAssignedDepartmentId(),
			projection.getAssignedDepartmentName(),
			projection.getStatus(),
			remainingHours,
			expired,
			editable,
			deletable,
			projection.getAssignedAt(),
			projection.getCreatedAt(),
			projection.getCompletedAt(),
			firstFileUrl(files),
			files,
			toAnswer(projection)
		);
	}

	private static String firstFileUrl(List<TicketFileResponse> files) {
		return files == null || files.isEmpty() ? null : files.get(0).fileUrl();
	}

	private static Answer toAnswer(MyTicketDetailProjection projection) {
		if (projection.getAnswerId() == null) {
			return null;
		}

		return new Answer(
			projection.getAnswerId(),
			projection.getAnswerContent(),
			projection.getAnswerAuthorId(),
			projection.getAnswerAuthorNickname(),
			projection.getAnswerAuthorDepartmentId(),
			projection.getAnswerAuthorDepartmentName(),
			projection.getAnsweredAt()
		);
	}
}
