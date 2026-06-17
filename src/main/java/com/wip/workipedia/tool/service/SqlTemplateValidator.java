package com.wip.workipedia.tool.service;

import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class SqlTemplateValidator {

	// OUTFILE/DUMPFILE(MariaDB 파일 쓰기), UNION/INFORMATION_SCHEMA(다른 테이블/스키마 메타데이터 탐색),
	// LOAD_FILE/BENCHMARK/SLEEP(파일 읽기·DoS성 함수)까지 차단해 텍스트 매칭 기반 검증의 명백한 우회를 줄인다.
	private static final Set<String> FORBIDDEN_KEYWORDS = Set.of(
		"INSERT", "UPDATE", "DELETE", "MERGE", "ALTER", "DROP", "TRUNCATE", "CREATE", "CALL", "EXEC", "EXECUTE",
		"UNION", "OUTFILE", "DUMPFILE", "LOAD_FILE", "BENCHMARK", "SLEEP", "INFORMATION_SCHEMA"
	);

	private static final Set<Pattern> FORBIDDEN_KEYWORD_PATTERNS = FORBIDDEN_KEYWORDS.stream()
		.map(keyword -> Pattern.compile("\\b" + keyword + "\\b"))
		.collect(java.util.stream.Collectors.toUnmodifiableSet());

	public ValidationResult validate(String queryTemplate) {
		if (queryTemplate == null || queryTemplate.isBlank()) {
			return ValidationResult.invalid("queryTemplate이 비어 있습니다.");
		}

		String trimmed = queryTemplate.trim();
		String upper = trimmed.toUpperCase();

		if (!upper.startsWith("SELECT")) {
			return ValidationResult.invalid("SELECT 쿼리만 허용됩니다.");
		}
		if (trimmed.contains(";")) {
			return ValidationResult.invalid("세미콜론(;)은 허용되지 않습니다.");
		}
		if (trimmed.contains("--") || trimmed.contains("/*") || trimmed.contains("*/") || trimmed.contains("#")) {
			return ValidationResult.invalid("SQL 주석은 허용되지 않습니다.");
		}
		for (Pattern pattern : FORBIDDEN_KEYWORD_PATTERNS) {
			if (pattern.matcher(upper).find()) {
				return ValidationResult.invalid("허용되지 않은 키워드입니다.");
			}
		}
		if (!upper.contains("LIMIT")) {
			return ValidationResult.invalid("LIMIT 절이 필요합니다.");
		}

		return ValidationResult.ok();
	}

	public record ValidationResult(boolean valid, String message) {
		// 레코드 컴포넌트 valid의 자동 접근자 valid()와 이름이 충돌해 static 팩토리 메서드명은 ok()로 둔다.
		public static ValidationResult ok() {
			return new ValidationResult(true, null);
		}

		public static ValidationResult invalid(String message) {
			return new ValidationResult(false, message);
		}
	}
}
