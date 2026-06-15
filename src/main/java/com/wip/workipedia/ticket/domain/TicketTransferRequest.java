package com.wip.workipedia.ticket.domain;

import com.wip.workipedia.common.domain.BaseTimeEntity;
import com.wip.workipedia.common.domain.ModifiedSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ticket_transfer_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketTransferRequest extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long transferRequestId;

	@Column(nullable = false)
	private Long ticketId;

	@Column(nullable = false)
	private Long requesterId;

	@Column(nullable = false)
	private Long fromDepartmentId;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String reason;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private TicketTransferRequestStatus status;

	@Column(nullable = false, length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
	private String isDeleted = "N";

	public static TicketTransferRequest create(
		Long ticketId,
		Long requesterId,
		Long fromDepartmentId,
		String reason
	) {
		TicketTransferRequest request = new TicketTransferRequest();
		request.ticketId = ticketId;
		request.requesterId = requesterId;
		request.fromDepartmentId = fromDepartmentId;
		request.reason = reason;
		request.status = TicketTransferRequestStatus.REQUESTED;
		request.touchModifiedSource(ModifiedSource.ADMIN);
		return request;
	}

	public void markAssignedFromQueue() {
		this.status = TicketTransferRequestStatus.ASSIGNED_FROM_QUEUE;
		touchModifiedSource(ModifiedSource.ADMIN);
	}

	public void delete() {
		markDeleted();
		this.isDeleted = "Y";
		touchModifiedSource(ModifiedSource.ADMIN);
	}
}
