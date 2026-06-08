package com.wip.workipedia.department.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "department_routing_prompts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DepartmentRoutingPrompt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "routing_prompt_id")
	private Long routingPromptId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "department_id", nullable = false)
	private Department department;

	@Column(name = "prompt_content", nullable = false, columnDefinition = "TEXT")
	private String promptContent;

	@Column(name = "is_active", nullable = false, length = 1, columnDefinition = "CHAR(1) DEFAULT 'Y'")
	private String isActive = "Y";

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Column(name = "is_deleted", nullable = false, length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
	private String isDeleted = "N";

	public static DepartmentRoutingPrompt create(Department department, String promptContent) {
		DepartmentRoutingPrompt routingPrompt = new DepartmentRoutingPrompt();
		routingPrompt.department = department;
		routingPrompt.promptContent = promptContent;
		return routingPrompt;
	}

	public void update(String promptContent) {
		this.promptContent = promptContent;
		this.isActive = "Y";
	}

	public void markDeleted() {
		this.deletedAt = LocalDateTime.now();
		this.isDeleted = "Y";
		this.isActive = "N";
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
