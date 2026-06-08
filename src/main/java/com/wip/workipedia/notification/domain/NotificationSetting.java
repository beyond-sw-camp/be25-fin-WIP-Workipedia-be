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

	public static NotificationSetting createDefault(Long userId) {
		return new NotificationSetting(userId, true, true, true);
	}

	public boolean isAllEnabled() {
		return ticketEnabled && workiEnabled && manualEnabled;
	}

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
