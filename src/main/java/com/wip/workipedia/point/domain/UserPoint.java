package com.wip.workipedia.point.domain;

import com.wip.workipedia.common.domain.BaseTimeEntity;
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
}
