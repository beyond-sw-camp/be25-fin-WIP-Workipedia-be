package com.wip.workipedia.admin.aisync.dto;

public record AiSyncJobStatsResponse(
    long pending,
    long processing,
    long synced,
    long failed
) {}
