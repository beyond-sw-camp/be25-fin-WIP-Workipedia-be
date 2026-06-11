package com.wip.workipedia.esg.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "esg_grade")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EsgGrade {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer gradeId;

	@Column(nullable = false, length = 20)
	private String gradeName;

	@Column(nullable = false)
	private long minScore;

	private Long maxScore;

	@Column(length = 500)
	private String gradeImageUrl;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	private LocalDateTime deletedAt;

	@Column(nullable = false, length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
	private String isDeleted = "N";
}
