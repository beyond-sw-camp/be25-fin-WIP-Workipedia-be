package com.wip.workipedia.admin.aisync.dto;

public record AiSyncCleanupResponse(int deleted, int skipped, int failed) {}
