package com.wip.workipedia.departmentsync.dto;

import java.util.List;

public record SyncPreviewResponse(List<SyncDiffRow> rows, int created, int renamed, int deleted) {}
