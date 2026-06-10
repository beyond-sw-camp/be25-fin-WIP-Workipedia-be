package com.wip.workipedia.notification.dto;

import jakarta.validation.constraints.NotNull;

public record NotificationSettingRequest(
	@NotNull
	Boolean allEnabled,

	@NotNull
	Boolean ticketEnabled,

	@NotNull
	Boolean workiEnabled,

	@NotNull
	Boolean manualEnabled
) {
}
