package com.wip.workipedia.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_logs")
public class AdminLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "admin_log_id")
	private Long id;

	@Column(name = "actor_id", nullable = false)
	private Long actorId;

	@Enumerated(EnumType.STRING)
	@Column(name = "action_type", nullable = false, length = 50)
	private AdminActionType actionType;

	@Column(name = "target_type", length = 50)
	private String targetType;

	@Column(name = "target_id")
	private Long targetId;

	@Column(name = "description", length = 1000)
	private String description;

	@Column(name = "metadata_json", columnDefinition = "json")
	private String metadataJson;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	protected AdminLog() {
	}
}
