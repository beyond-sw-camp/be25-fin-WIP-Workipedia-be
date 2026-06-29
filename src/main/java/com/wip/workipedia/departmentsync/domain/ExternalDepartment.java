package com.wip.workipedia.departmentsync.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "external_departments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExternalDepartment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "external_department_id")
	private Long externalDepartmentId;

	@Column(name = "source_system", nullable = false, length = 50)
	private String sourceSystem;

	@Column(name = "external_id", nullable = false, length = 100)
	private String externalId;

	@Column(name = "department_name", nullable = false, length = 100)
	private String departmentName;

	@Column(name = "parent_external_id", length = 100)
	private String parentExternalId;

	@Column(name = "duty_desc", columnDefinition = "TEXT")
	private String dutyDesc;

	@Column(name = "use_yn", nullable = false, length = 1, columnDefinition = "CHAR(1)")
	private String useYn = "Y";

	@Column(name = "raw_payload", columnDefinition = "JSON")
	private String rawPayload;

	@Column(name = "mapped_department_id")
	private Long mappedDepartmentId;

	@Enumerated(EnumType.STRING)
	@Column(name = "sync_state", nullable = false, length = 20)
	private SyncState syncState = SyncState.NEW;

	@Column(name = "fetched_at", nullable = false)
	private LocalDateTime fetchedAt;

	@Column(name = "applied_at")
	private LocalDateTime appliedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	public static ExternalDepartment stage(String sourceSystem, String externalId, String departmentName,
			String parentExternalId, String dutyDesc, String useYn, String rawPayload) {
		ExternalDepartment e = new ExternalDepartment();
		e.sourceSystem = sourceSystem;
		e.externalId = externalId;
		e.departmentName = departmentName;
		e.parentExternalId = parentExternalId;
		e.dutyDesc = dutyDesc;
		e.useYn = (useYn == null || useYn.isBlank()) ? "Y" : useYn;
		e.rawPayload = rawPayload;
		e.syncState = SyncState.NEW;
		e.fetchedAt = LocalDateTime.now();
		return e;
	}

	public void refreshFrom(String departmentName, String parentExternalId, String dutyDesc,
			String useYn, String rawPayload) {
		this.departmentName = departmentName;
		this.parentExternalId = parentExternalId;
		this.dutyDesc = dutyDesc;
		this.useYn = (useYn == null || useYn.isBlank()) ? "Y" : useYn;
		this.rawPayload = rawPayload;
		this.fetchedAt = LocalDateTime.now();
	}

	public void assignState(SyncState state) {
		this.syncState = state;
	}

	public void markApplied(Long mappedDepartmentId) {
		this.mappedDepartmentId = mappedDepartmentId;
		this.syncState = SyncState.APPLIED;
		this.appliedAt = LocalDateTime.now();
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
