package com.wip.workipedia.point.domain;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
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
@Table(name = "point_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pointHistoryId;

	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false)
	private int pointAmount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PointHistoryType type;

	@Column(nullable = false, length = 50)
	private String reasonType;

	@Column(length = 50)
	private String relatedType;

	private Long relatedId;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	private LocalDateTime deletedAt;

	@Column(nullable = false, length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
	private String isDeleted = "N";

	private PointHistory(Long userId, int pointAmount, PointHistoryType type,
			String reasonType, String relatedType, Long relatedId) {
		this.userId = userId;
		this.pointAmount = pointAmount;
		this.type = type;
		this.reasonType = reasonType;
		this.relatedType = relatedType;
		this.relatedId = relatedId;
		this.createdAt = LocalDateTime.now();
	}

	public static PointHistory earn(Long userId, int amount, String reasonType, String relatedType, Long relatedId) {
		validatePositiveAmount(amount);
		return new PointHistory(userId, amount, PointHistoryType.EARN, reasonType, relatedType, relatedId);
	}

	public static PointHistory spend(Long userId, int amount, String reasonType, String relatedType, Long relatedId) {
		validatePositiveAmount(amount);
		return new PointHistory(userId, -amount, PointHistoryType.SPEND, reasonType, relatedType, relatedId);
	}

	public static PointHistory reset(Long userId, int pointAmount, String reasonType, String relatedType, Long relatedId) {
		return new PointHistory(userId, pointAmount, PointHistoryType.RESET, reasonType, relatedType, relatedId);
	}

	private static void validatePositiveAmount(int amount) {
		if (amount <= 0) {
			throw new CustomException(ErrorType.POINT_INVALID_AMOUNT);
		}
	}
}
