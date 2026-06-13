package com.wip.workipedia.ticket.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tickets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ticket {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long ticketId;

	@Column(nullable = false)
	private Long requesterId;

	private Long sourceChatbotMessageId;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TicketPriority priority;

	private Long assigneeId;
	private Long assignedDepartmentId;
	private LocalDateTime assignedAt;

	@Column(precision = 5, scale = 2)
	private BigDecimal routingConfidenceScore;

	@Enumerated(EnumType.STRING)
	private RoutingDecision routingDecision;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private TicketStatus status;

	private LocalDateTime completedAt;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	private LocalDateTime deletedAt;

	@Column(nullable = false, length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
	private String isDeleted = "N";

	public static Ticket create(
		Long requesterId,
		Long sourceChatbotMessageId,
		TicketPriority priority,
		String title,
		String content
	) {
		LocalDateTime now = LocalDateTime.now();

		Ticket ticket = new Ticket();
		ticket.requesterId = requesterId;
		ticket.sourceChatbotMessageId = sourceChatbotMessageId;
		ticket.priority = priority;
		ticket.title = title;
		ticket.content = content;
		ticket.status = TicketStatus.COMMON_QUEUE;
		ticket.createdAt = now;
		ticket.updatedAt = now;
		return ticket;
	}

	public void applyRouting(Long departmentId, String departmentName, BigDecimal confidenceScore, RoutingDecision decision) {
		this.assignedDepartmentId = departmentId;
		this.assignedAt = departmentId == null ? null : LocalDateTime.now();
		this.routingConfidenceScore = confidenceScore;
		this.routingDecision = decision;
		this.status = decision == RoutingDecision.AUTO_ASSIGNED ? TicketStatus.ASSIGNED : TicketStatus.COMMON_QUEUE;
		touch();
	}

	public void changeStatus(TicketStatus status) {
		this.status = status;
		touch();
	}

	public void assignTo(Long assigneeId) {
		this.assigneeId = assigneeId;
		touch();
	}

	public void assignDepartment(Long departmentId) {
		this.assignedDepartmentId = departmentId;
		this.assigneeId = null;
		this.assignedAt = LocalDateTime.now();
		this.routingDecision = RoutingDecision.ADMIN_REVIEW;
		this.status = TicketStatus.ASSIGNED;
		touch();
	}

	public void transferToCommonQueue() {
		this.assigneeId = null;
		this.assignedDepartmentId = null;
		this.assignedAt = null;
		this.routingDecision = RoutingDecision.COMMON_QUEUE;
		this.status = TicketStatus.COMMON_QUEUE;
		touch();
	}

	public void complete() {
		this.status = TicketStatus.COMPLETED;
		this.completedAt = LocalDateTime.now();
		touch();
	}

	private void touch() {
		this.updatedAt = LocalDateTime.now();
	}
}
