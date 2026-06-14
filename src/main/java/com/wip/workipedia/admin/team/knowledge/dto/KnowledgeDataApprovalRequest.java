package com.wip.workipedia.admin.team.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record KnowledgeDataApprovalRequest(
	@NotBlank
	@Size(max = 255)
	String question,

	@NotBlank
	@Size(max = 10000)
	String answer
) {
}
