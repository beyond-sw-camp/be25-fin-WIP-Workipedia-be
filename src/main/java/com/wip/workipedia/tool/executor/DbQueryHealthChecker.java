package com.wip.workipedia.tool.executor;

import com.wip.workipedia.tool.domain.AiTool;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DbQueryHealthChecker {

	private final Map<String, NamedParameterJdbcTemplate> toolJdbcTemplates;

	public HealthCheckResult check(AiTool tool) {
		NamedParameterJdbcTemplate jdbcTemplate = toolJdbcTemplates.get(tool.getDatasourceKey());
		if (jdbcTemplate == null) {
			return HealthCheckResult.failure("허용되지 않은 datasource입니다: " + tool.getDatasourceKey());
		}

		long startedAt = System.currentTimeMillis();
		try {
			jdbcTemplate.queryForObject("SELECT 1", Map.of(), Integer.class);
			return HealthCheckResult.success(System.currentTimeMillis() - startedAt);
		} catch (DataAccessException e) {
			return HealthCheckResult.failure(System.currentTimeMillis() - startedAt, "DB 연결에 실패했습니다.");
		}
	}
}
