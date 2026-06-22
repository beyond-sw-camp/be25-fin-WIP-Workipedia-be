package com.wip.workipedia.tool.executor;

import com.wip.workipedia.tool.domain.AiTool;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DbQueryHealthCheckerTest {

	@Mock NamedParameterJdbcTemplate jdbcTemplate;

	private AiTool dbQueryTool() {
		return AiTool.createDbQueryTool(
			"휴가잔여일조회", "설명", "workipediaReadonly",
			"SELECT name FROM employee_vacations LIMIT 1",
			"{\"properties\":{}}", null, 3000, 10, 1L
		);
	}

	@Test
	void check_SELECT_1_성공하면_성공() {
		DbQueryHealthChecker checker = new DbQueryHealthChecker(Map.of("workipediaReadonly", jdbcTemplate));
		given(jdbcTemplate.queryForObject("SELECT 1", Map.of(), Integer.class)).willReturn(1);

		HealthCheckResult result = checker.check(dbQueryTool());

		assertThat(result.success()).isTrue();
	}

	@Test
	void check_allowlist에_없는_datasource면_실패() {
		DbQueryHealthChecker checker = new DbQueryHealthChecker(Map.of());

		HealthCheckResult result = checker.check(dbQueryTool());

		assertThat(result.success()).isFalse();
	}

	@Test
	void check_DataAccessException_발생시_실패() {
		DbQueryHealthChecker checker = new DbQueryHealthChecker(Map.of("workipediaReadonly", jdbcTemplate));
		given(jdbcTemplate.queryForObject("SELECT 1", Map.of(), Integer.class))
			.willThrow(new QueryTimeoutException("timeout"));

		HealthCheckResult result = checker.check(dbQueryTool());

		assertThat(result.success()).isFalse();
	}
}
