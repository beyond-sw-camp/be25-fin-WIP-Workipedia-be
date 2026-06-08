package com.wip.workipedia.department.ai;

import java.util.List;

public interface DepartmentRoutingPromptEditor {

	List<RoutingPromptEditResult> edit(List<RoutingPromptEditTarget> targets, String instruction);
}
