package com.wip.workipedia.department.ai;

public record KnowledgeSyncRequest(
	Long sourceId,
	String sourceType,
	String title,
	String content,
	Long departmentId,
	String departmentName
) {
	public static KnowledgeSyncRequest ofDeptRr(Long departmentId, String departmentName, String routingPrompt) {
		return new KnowledgeSyncRequest(
			departmentId,
			"DEPT_RR",
			departmentName,
			routingPrompt,
			departmentId,
			departmentName
		);
	}
}
