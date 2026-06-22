package com.wip.workipedia.tool.executor;

import org.springframework.web.client.RestClient;

public interface ToolRestClientFactory {
	RestClient create(long timeoutMs);
}
