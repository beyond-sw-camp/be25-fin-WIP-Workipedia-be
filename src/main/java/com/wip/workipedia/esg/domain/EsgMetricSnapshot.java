package com.wip.workipedia.esg.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "esg_metric_snapshots")
public class EsgMetricSnapshot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "esg_metric_snapshot_id")
	private Long id;

	@Column(name = "target_type", nullable = false, length = 30)
	private String targetType;

	@Column(name = "target_id")
	private Long targetId;

	@Column(name = "knowledge_share_count", nullable = false)
	private long knowledgeShareCount;

	@Column(name = "accepted_answer_count", nullable = false)
	private long acceptedAnswerCount;

	@Column(name = "estimated_saved_minutes", nullable = false)
	private long estimatedSavedMinutes;

	@Column(name = "source_backed_answer_rate", nullable = false, precision = 5, scale = 4)
	private BigDecimal sourceBackedAnswerRate;

	@Column(name = "ticket_completion_rate", nullable = false, precision = 5, scale = 4)
	private BigDecimal ticketCompletionRate;

	@Column(name = "measured_date", nullable = false)
	private LocalDate measuredDate;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	protected EsgMetricSnapshot() {
	}
}
