package com.wip.workipedia.point.domain;

import com.wip.workipedia.common.domain.BaseTimeEntity;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_points")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPoint extends BaseTimeEntity {
	private static final int DEFAULT_GRADE_ID = 1;

	@Id
	private Long userId;

	@Column(nullable = false)
	private Integer gradeId;

	@Column(nullable = false)
	private long currentPoint;

	@Column(nullable = false)
	private long esgScore;

	@Column(nullable = false, length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
	private String isDeleted = "N";

	private UserPoint(Long userId) {
		this.userId = userId;
		this.gradeId = DEFAULT_GRADE_ID;
		this.currentPoint = 0;
		this.esgScore = 0;
	}

	public static UserPoint create(Long userId) {
		return new UserPoint(userId);
	}

	public void earn(long amount) {
		validatePositiveAmount(amount);
		this.currentPoint += amount;
		this.esgScore += amount;
	}

	public void spend(long amount) {
		validatePositiveAmount(amount);
		if (this.currentPoint < amount) {
			throw new CustomException(ErrorType.POINT_INSUFFICIENT_BALANCE);
		}
		this.currentPoint -= amount;
	}

	public long reset() {
		long resetAmount = this.currentPoint;
		this.currentPoint = 0;
		this.esgScore = 0;
		return resetAmount;
	}

	private static void validatePositiveAmount(long amount) {
		if (amount <= 0) {
			throw new CustomException(ErrorType.POINT_INVALID_AMOUNT);
		}
	}
}
