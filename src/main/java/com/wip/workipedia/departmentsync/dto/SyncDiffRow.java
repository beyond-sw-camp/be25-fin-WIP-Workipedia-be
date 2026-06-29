package com.wip.workipedia.departmentsync.dto;

import com.wip.workipedia.departmentsync.domain.SyncState;

public record SyncDiffRow(
	String externalId,
	String departmentName,
	String previousName,
	SyncState state,
	long memberMoveCount,
	Long mappedDepartmentId
) {}
