package com.wip.workipedia.point.domain;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "points_daily_limit")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointsDailyLimit {

	@EmbeddedId
	private PointsDailyLimitId id;

	@Column(nullable = false)
	private long todayPoint;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	private LocalDateTime deletedAt;

	@Column(nullable = false, length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
	private String isDeleted = "N";

	private PointsDailyLimit(Long userId, LocalDate pointDate) {
		this.id = new PointsDailyLimitId(userId, pointDate);
		this.todayPoint = 0;
	}

	public static PointsDailyLimit create(Long userId, LocalDate pointDate) {
		return new PointsDailyLimit(userId, pointDate);
	}

	public long remainingPoint(long dailyLimit) {
		return Math.max(0, dailyLimit - todayPoint);
	}

	public void addPoint(long amount) {
		if (amount <= 0) {
			throw new CustomException(ErrorType.POINT_INVALID_AMOUNT);
		}
		this.todayPoint += amount;
	}

	@PrePersist
	void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
