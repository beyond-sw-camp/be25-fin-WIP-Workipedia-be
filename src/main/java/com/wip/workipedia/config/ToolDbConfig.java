package com.wip.workipedia.config;

import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
@EnableConfigurationProperties(ToolDbProperties.class)
public class ToolDbConfig {

	@Bean
	public Map<String, NamedParameterJdbcTemplate> toolJdbcTemplates(ToolDbProperties properties) {
		Set<String> allowed = properties.allowedDatasources() == null
			? Set.of()
			: new HashSet<>(properties.allowedDatasources());

		Map<String, NamedParameterJdbcTemplate> templates = new HashMap<>();
		if (properties.datasources() == null) {
			return templates;
		}

		properties.datasources().forEach((key, config) -> {
			if (!allowed.contains(key)) {
				return;
			}
			HikariDataSource dataSource = new HikariDataSource();
			dataSource.setJdbcUrl(config.url());
			dataSource.setUsername(config.username());
			dataSource.setPassword(config.password());
			dataSource.setMaximumPoolSize(2);
			dataSource.setReadOnly(true);
			templates.put(key, new NamedParameterJdbcTemplate(dataSource));
		});

		return templates;
	}
}
