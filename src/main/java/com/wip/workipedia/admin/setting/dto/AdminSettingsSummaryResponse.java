package com.wip.workipedia.admin.setting.dto;

public record AdminSettingsSummaryResponse(
	long totalUserCount,
	long todayLoginCount,
	long totalDocumentCount
) {
}
