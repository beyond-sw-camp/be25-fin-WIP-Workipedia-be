package com.wip.workipedia.tool.executor;

public interface SsrfGuard {
	boolean isSafe(String endpointUrl);
}
