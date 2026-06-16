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
@Table(name = "ticket_routing_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketRoutingLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long routingLogId;

	@Column(nullable = false)
	private Long ticketId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private RoutingDecision decision;

	@Column(precision = 5, scale = 2)
	private BigDecimal confidenceScore;

	@Column(precision = 5, scale = 2)
	private BigDecimal scoreMargin;

	@Column(columnDefinition = "JSON")
	private String candidateDepartmentsJson;

	@Column(columnDefinition = "JSON")
	private String reasonsJson;

	@Column(length = 100)
	private String modelVersion;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public static TicketRoutingLog create(
		Long ticketId,
		RoutingDecision decision,
		BigDecimal confidenceScore,
		BigDecimal scoreMargin,
		String candidateDepartmentsJson,
		String reasonsJson,
		String modelVersion
	) {
		TicketRoutingLog log = new TicketRoutingLog();
		log.ticketId = ticketId;
		log.decision = decision;
		log.confidenceScore = confidenceScore;
		log.scoreMargin = scoreMargin;
		log.candidateDepartmentsJson = candidateDepartmentsJson;
		log.reasonsJson = reasonsJson;
		log.modelVersion = modelVersion;
		log.createdAt = LocalDateTime.now();
		return log;
	}
}
