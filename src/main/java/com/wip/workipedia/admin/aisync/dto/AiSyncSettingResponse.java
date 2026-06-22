package com.wip.workipedia.admin.aisync.dto;

import com.wip.workipedia.aisync.domain.AiSyncSetting;

public record AiSyncSettingResponse(int retentionDays) {
    public static AiSyncSettingResponse from(AiSyncSetting setting) {
        return new AiSyncSettingResponse(setting.getRetentionDays());
    }
}
