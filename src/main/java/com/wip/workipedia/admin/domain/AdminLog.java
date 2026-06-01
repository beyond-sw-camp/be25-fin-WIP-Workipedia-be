package com.wip.workipedia.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_logs")
public class AdminLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "admin_log_id")
	private Long id;

	// 작업을 수행한 관리자 사용자 ID입니다.
	@Column(name = "actor_id", nullable = false)
	private Long actorId;

	// 어떤 종류의 관리자 작업인지 enum 문자열로 저장합니다.
	@Enumerated(EnumType.STRING)
	@Column(name = "action_type", nullable = false, length = 50)
	private AdminActionType actionType;

	// 작업 대상 타입입니다. 예: TICKET, WORKI, USER, KNOWLEDGE_CANDIDATE.
	@Column(name = "target_type", length = 50)
	private String targetType;

	// 작업 대상의 PK입니다. 대상이 없거나 시스템 작업이면 null일 수 있습니다.
	@Column(name = "target_id")
	private Long targetId;

	@Column(name = "description", length = 1000)
	private String description;

	// 화면 표시에는 필요 없지만, 재배정 사유 같은 부가 정보를 JSON으로 남길 수 있습니다.
	@Column(name = "metadata_json", columnDefinition = "json")
	private String metadataJson;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	// JPA가 엔티티를 만들 때 사용하는 기본 생성자입니다.
	protected AdminLog() {
	}
}
