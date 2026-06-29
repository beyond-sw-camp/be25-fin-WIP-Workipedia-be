package com.wip.workipedia.departmentsync.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SyncPreviewRequest(
	@NotBlank String sourceSystem,
	@NotEmpty @Valid List<ErpDepartmentItem> items
) {}
