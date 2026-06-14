package com.wip.workipedia.ticket.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ticket_transfer_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketTransferRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long transferRequestId;

	@Column(nullable = false)
	private Long ticketId;

	@Column(nullable = false)
	private Long requesterId;

	@Column(nullable = false)
	private Long fromDepartmentId;

	private Long suggestedDepartmentId;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String reason;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private TicketTransferRequestStatus status;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	private LocalDateTime deletedAt;

	@Column(nullable = false, length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
	private String isDeleted = "N";

	public static TicketTransferRequest create(
		Long ticketId,
		Long requesterId,
		Long fromDepartmentId,
		Long suggestedDepartmentId,
		String reason
	) {
		LocalDateTime now = LocalDateTime.now();
		TicketTransferRequest request = new TicketTransferRequest();
		request.ticketId = ticketId;
		request.requesterId = requesterId;
		request.fromDepartmentId = fromDepartmentId;
		request.suggestedDepartmentId = suggestedDepartmentId;
		request.reason = reason;
		request.status = TicketTransferRequestStatus.REQUESTED;
		request.createdAt = now;
		request.updatedAt = now;
		return request;
	}

	public void markAssignedFromQueue() {
		this.status = TicketTransferRequestStatus.ASSIGNED_FROM_QUEUE;
		this.updatedAt = LocalDateTime.now();
	}
}
