package com.wip.workipedia.config;

import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("tool.db")
public record ToolDbProperties(List<String> allowedDatasources, Map<String, DatasourceConfig> datasources) {

	public record DatasourceConfig(String url, String username, String password) {
	}
}
