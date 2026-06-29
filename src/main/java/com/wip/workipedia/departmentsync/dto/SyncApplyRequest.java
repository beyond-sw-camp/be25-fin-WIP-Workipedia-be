package com.wip.workipedia.departmentsync.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SyncApplyRequest(
	@NotBlank String sourceSystem,
	@NotEmpty @Valid List<ErpDepartmentItem> items,
	List<MergeResolution> merges,
	List<ManualLink> manualLinks,
	Long reassignTargetDepartmentId
) {}
