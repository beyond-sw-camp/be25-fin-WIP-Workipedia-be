package com.wip.workipedia.admin.aisync.dto;

import jakarta.validation.constraints.Min;

// retentionDays = 0 은 "기한 없음" — 자동 정리를 수행하지 않는다.
public record AiSyncSettingUpdateRequest(@Min(0) int retentionDays) {}
