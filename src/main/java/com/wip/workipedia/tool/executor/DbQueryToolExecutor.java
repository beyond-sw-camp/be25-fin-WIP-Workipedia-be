package com.wip.workipedia.tool.executor;

import com.wip.workipedia.tool.domain.AiTool;
import com.wip.workipedia.tool.exception.ToolExecutionException;
import com.wip.workipedia.tool.service.SqlTemplateValidator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DbQueryToolExecutor {

	private final Map<String, NamedParameterJdbcTemplate> toolJdbcTemplates;
	private final SqlTemplateValidator sqlTemplateValidator;

	public ToolExecutionResult execute(AiTool tool, Map<String, Object> parameters) {
		NamedParameterJdbcTemplate jdbcTemplate = toolJdbcTemplates.get(tool.getDatasourceKey());
		if (jdbcTemplate == null) {
			throw new ToolExecutionException(
				"DATASOURCE_NOT_ALLOWED", "허용되지 않은 datasource입니다: " + tool.getDatasourceKey()
			);
		}

		SqlTemplateValidator.ValidationResult validation = sqlTemplateValidator.validate(tool.getQueryTemplate());
		if (!validation.valid()) {
			throw new ToolExecutionException("INVALID_QUERY_TEMPLATE", validation.message());
		}

		try {
			List<Map<String, Object>> rows = jdbcTemplate.queryForList(tool.getQueryTemplate(), escapeLikeWildcards(parameters));
			List<Map<String, Object>> truncated = rows.size() > tool.getMaxResultCount()
				? rows.subList(0, tool.getMaxResultCount())
				: rows;
			return new ToolExecutionResult(truncated, truncated.size());
		} catch (DataAccessException e) {
			throw new ToolExecutionException("DB_QUERY_ERROR", "DB 쿼리 실행에 실패했습니다.");
		}
	}

	// queryTemplate이 LIKE를 쓰는 경우, AI가 채운 값에 '%'/'_'가 그대로 들어가면 의도한 범위를 벗어나
	// 전체 테이블을 매칭시킬 수 있다(예: name="%"). '='로 비교하는 쿼리는 어차피 와일드카드를 해석하지
	// 않으므로, 모든 문자열 파라미터에 escape를 걸어도 안전하다.
	// MariaDB는 NO_BACKSLASH_ESCAPES가 꺼져 있는 한 백슬래시를 LIKE의 기본 escape 문자로 인식한다.
	private Map<String, Object> escapeLikeWildcards(Map<String, Object> parameters) {
		Map<String, Object> escaped = new LinkedHashMap<>();
		parameters.forEach((key, value) -> escaped.put(key, value instanceof String s ? escapeLikeValue(s) : value));
		return escaped;
	}

	private String escapeLikeValue(String value) {
		return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}
}
