package com.wip.workipedia.admin.aitool.dto;

import java.util.List;

public record AiToolDraftResponse(
	String name,
	String description,
	String endpointUrl,
	List<Parameter> parameters
) {
	public record Parameter(
		String name,
		String type,
		String description,
		boolean required
	) {
	}
}
