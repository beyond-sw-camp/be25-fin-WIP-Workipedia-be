package com.wip.workipedia.department.ai;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class FallbackDepartmentRoutingPromptEditor implements DepartmentRoutingPromptEditor {

	@Override
	public String edit(String departmentName, String currentPrompt, String instruction) {
		if (!StringUtils.hasText(currentPrompt)) {
			return instruction.trim();
		}

		return currentPrompt.trim()
			+ System.lineSeparator()
			+ instruction.trim();
	}
}
