package com.wip.workipedia.tool.executor;

import com.wip.workipedia.tool.domain.AiTool;
import com.wip.workipedia.tool.exception.ToolExecutionException;
import com.wip.workipedia.tool.service.SqlTemplateValidator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DbQueryToolExecutorTest {

	@Mock NamedParameterJdbcTemplate jdbcTemplate;

	private final SqlTemplateValidator sqlTemplateValidator = new SqlTemplateValidator();
	private DbQueryToolExecutor executor;

	@BeforeEach
	void setUp() {
		executor = new DbQueryToolExecutor(Map.of("workipediaReadonly", jdbcTemplate), sqlTemplateValidator);
	}

	private AiTool dbQueryTool(String datasourceKey, String queryTemplate, int maxResultCount) {
		return AiTool.createDbQueryTool(
			"휴가잔여일조회", "직원 휴가 잔여일을 조회합니다.",
			datasourceKey, queryTemplate, "{\"properties\":{}}", null, 3000, maxResultCount, 1L
		);
	}

	@Test
	void execute_허용된_datasource면_NamedParameterJdbcTemplate으로_실행() {
		AiTool tool = dbQueryTool(
			"workipediaReadonly",
			"SELECT name FROM employee_vacations WHERE employee_id = :employeeId LIMIT 1",
			10
		);
		given(jdbcTemplate.queryForList(tool.getQueryTemplate(), Map.of("employeeId", "E001")))
			.willReturn(List.of(Map.of("name", "홍길동")));

		ToolExecutionResult result = executor.execute(tool, Map.of("employeeId", "E001"));

		assertThat(result.resultCount()).isEqualTo(1);
	}

	@Test
	void execute_allowlist에_없는_datasourceKey면_ToolExecutionException() {
		AiTool tool = dbQueryTool("unknownDatasource", "SELECT name FROM employee_vacations LIMIT 1", 10);

		assertThatThrownBy(() -> executor.execute(tool, Map.of()))
			.isInstanceOf(ToolExecutionException.class)
			.satisfies(e -> assertThat(((ToolExecutionException) e).getErrorCode()).isEqualTo("DATASOURCE_NOT_ALLOWED"));
	}

	@Test
	void execute_SELECT가_아닌_queryTemplate이면_ToolExecutionException() {
		AiTool tool = dbQueryTool("workipediaReadonly", "DELETE FROM employee_vacations LIMIT 1", 10);

		assertThatThrownBy(() -> executor.execute(tool, Map.of()))
			.isInstanceOf(ToolExecutionException.class)
			.satisfies(e -> assertThat(((ToolExecutionException) e).getErrorCode()).isEqualTo("INVALID_QUERY_TEMPLATE"));
	}

	@Test
	void execute_결과가_maxResultCount보다_많으면_잘림() {
		AiTool tool = dbQueryTool("workipediaReadonly", "SELECT name FROM employee_vacations LIMIT 10", 1);
		given(jdbcTemplate.queryForList(tool.getQueryTemplate(), Map.of()))
			.willReturn(List.of(Map.of("name", "a"), Map.of("name", "b")));

		ToolExecutionResult result = executor.execute(tool, Map.of());

		assertThat(result.resultCount()).isEqualTo(1);
	}

	@Test
	void execute_파라미터값의_LIKE_와일드카드는_escape되어_전달() {
		AiTool tool = dbQueryTool(
			"workipediaReadonly",
			"SELECT name FROM employee_vacations WHERE name LIKE CONCAT('%', :name, '%') LIMIT 10",
			10
		);
		// "%"/"_"를 그대로 넘기면 LIKE가 와일드카드로 해석해버리므로, executor가 \%, \_로 escape해서 바인딩해야 한다.
		given(jdbcTemplate.queryForList(tool.getQueryTemplate(), Map.of("name", "100\\%\\_off")))
			.willReturn(List.of());

		ToolExecutionResult result = executor.execute(tool, Map.of("name", "100%_off"));

		assertThat(result.resultCount()).isEqualTo(0);
	}

	@Test
	void execute_DataAccessException_발생시_ToolExecutionException() {
		AiTool tool = dbQueryTool("workipediaReadonly", "SELECT name FROM employee_vacations LIMIT 10", 10);
		given(jdbcTemplate.queryForList(tool.getQueryTemplate(), Map.of()))
			.willThrow(new QueryTimeoutException("timeout"));

		assertThatThrownBy(() -> executor.execute(tool, Map.of()))
			.isInstanceOf(ToolExecutionException.class)
			.satisfies(e -> assertThat(((ToolExecutionException) e).getErrorCode()).isEqualTo("DB_QUERY_ERROR"));
	}
}
