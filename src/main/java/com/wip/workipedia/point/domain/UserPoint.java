package com.wip.workipedia.point.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_points")
public class UserPoint {

	@Id
	@Column(name = "user_id")
	private Long userId;

	@Column(name = "current_point", nullable = false)
	private long currentPoint;

	@Column(name = "esg_score", nullable = false)
	private long esgScore;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	protected UserPoint() {
	}
}
