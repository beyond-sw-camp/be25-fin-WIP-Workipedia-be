package com.wip.workipedia.badge.dto;

import java.time.LocalDateTime;

public record BadgeResponse(
	long badgeId,
	String code,
	String name,
	String description,
	boolean earned,
	LocalDateTime earnedAt
) {
}
