package com.wip.workipedia.point.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "point_history")
public class PointHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "point_history_id")
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "point_amount", nullable = false)
	private int pointAmount;

	@Column(name = "reason_type", nullable = false, length = 50)
	private String reasonType;

	@Column(name = "related_type", length = 50)
	private String relatedType;

	@Column(name = "related_id")
	private Long relatedId;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	protected PointHistory() {
	}
}
