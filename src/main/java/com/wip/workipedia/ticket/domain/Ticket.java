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

	private Long questionId;
	private Long sourceChatbotMessageId;
	private Long categoryId;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TicketPriority priority;

	private Long assigneeId;
	private Long assignedDepartmentId;

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
		Long questionId,
		Long sourceChatbotMessageId,
		Long categoryId,
		TicketPriority priority,
		String title,
		String content
	) {
		LocalDateTime now = LocalDateTime.now();

		Ticket ticket = new Ticket();
		ticket.requesterId = requesterId;
		ticket.questionId = questionId;
		ticket.sourceChatbotMessageId = sourceChatbotMessageId;
		ticket.categoryId = categoryId;
		ticket.priority = priority;
		ticket.title = title;
		ticket.content = content;
		ticket.status = TicketStatus.RECEIVED;
		ticket.createdAt = now;
		ticket.updatedAt = now;
		return ticket;
	}

	public void applyRouting(Long departmentId, String departmentName, BigDecimal confidenceScore, RoutingDecision decision) {
		this.assignedDepartmentId = departmentId;
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
		this.status = TicketStatus.IN_PROGRESS;
		touch();
	}

	public void updateQuestion(String title, String content) {
		this.title = title;
		this.content = content;
		touch();
	}

	public void delete() {
		this.status = TicketStatus.DELETED;
		this.deletedAt = LocalDateTime.now();
		this.isDeleted = "Y";
		touch();
	}

	private void touch() {
		this.updatedAt = LocalDateTime.now();
	}
}
