package com.wip.workipedia.notification.domain;

import com.wip.workipedia.common.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "notification_settings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting extends BaseTimeEntity {

	@Id
	@Column(name = "user_id")
	private Long userId;

	@Column(name = "ticket_enabled", nullable = false)
	private boolean ticketEnabled;

	@Column(name = "worki_enabled", nullable = false)
	private boolean workiEnabled;

	@Column(name = "manual_enabled", nullable = false)
	private boolean manualEnabled;

	@Column(name = "is_deleted", nullable = false, length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
	private String isDeleted = "N";

	private NotificationSetting(
		Long userId,
		boolean ticketEnabled,
		boolean workiEnabled,
		boolean manualEnabled
	) {
		this.userId = userId;
		this.ticketEnabled = ticketEnabled;
		this.workiEnabled = workiEnabled;
		this.manualEnabled = manualEnabled;
	}

	// 알림 설정이 없는 사용자에게 기본 전체 ON 설정을 생성합니다.
	public static NotificationSetting createDefault(Long userId) {
		return new NotificationSetting(userId, true, true, true);
	}

	// 하위 알림 설정이 모두 켜져 있는 경우 전체 알림 설정을 ON으로 계산합니다.
	public boolean isAllEnabled() {
		return ticketEnabled && workiEnabled && manualEnabled;
	}

	// 사용자가 변경한 티켓, Worki, 매뉴얼 알림 설정값을 저장합니다.
	public void update(
		boolean ticketEnabled,
		boolean workiEnabled,
		boolean manualEnabled
	) {
		this.ticketEnabled = ticketEnabled;
		this.workiEnabled = workiEnabled;
		this.manualEnabled = manualEnabled;
	}
}
