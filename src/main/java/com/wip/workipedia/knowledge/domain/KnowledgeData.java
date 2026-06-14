package com.wip.workipedia.knowledge.domain;

import com.wip.workipedia.common.domain.ModifiedSource;
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
@Table(name = "knowledge_data")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KnowledgeData {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "knowledge_data_id")
	private Long knowledgeDataId;

	@Column(nullable = false)
	private Long ticketId;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, columnDefinition = "LONGTEXT")
	private String content;

	private Long departmentId;

	@Column(nullable = false)
	private Long approvedBy;

	@Column(nullable = false)
	private LocalDateTime approvedAt;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	private LocalDateTime deletedAt;

	@Column(nullable = false, length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
	private String isDeleted = "N";

	@Enumerated(EnumType.STRING)
	@Column(length = 30)
	private ModifiedSource modifiedSource;

	public static KnowledgeData approve(
		Long ticketId,
		String question,
		String answer,
		Long departmentId,
		Long approvedBy
	) {
		LocalDateTime now = LocalDateTime.now();
		KnowledgeData knowledgeData = new KnowledgeData();
		knowledgeData.ticketId = ticketId;
		knowledgeData.title = question;
		knowledgeData.content = answer;
		knowledgeData.departmentId = departmentId;
		knowledgeData.approvedBy = approvedBy;
		knowledgeData.approvedAt = now;
		knowledgeData.createdAt = now;
		knowledgeData.modifiedSource = ModifiedSource.ADMIN;
		return knowledgeData;
	}

	public void update(String question, String answer, Long actorUserId) {
		this.title = question;
		this.content = answer;
		this.approvedBy = actorUserId;
		this.updatedAt = LocalDateTime.now();
		this.modifiedSource = ModifiedSource.ADMIN;
	}

	public void delete(Long actorUserId) {
		this.deletedAt = LocalDateTime.now();
		this.updatedAt = this.deletedAt;
		this.isDeleted = "Y";
		this.approvedBy = actorUserId;
		this.modifiedSource = ModifiedSource.ADMIN;
	}
}
