package com.wip.workipedia.department.ai;

public interface KnowledgeSyncAiClient {
	void sync(KnowledgeSyncRequest request);
	void delete(Long sourceId, String sourceType);
}
