package com.wip.workipedia.point.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PointsDailyLimitId implements Serializable {

	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false)
	private LocalDate pointDate;
}
