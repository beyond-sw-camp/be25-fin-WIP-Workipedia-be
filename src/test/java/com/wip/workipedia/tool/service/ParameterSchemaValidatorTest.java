package com.wip.workipedia.tool.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ParameterSchemaValidatorTest {

	private final ParameterSchemaValidator validator = new ParameterSchemaValidator(new ObjectMapper());

	private static final String SCHEMA = """
		{"properties": {
			"employeeId": {"type": "string", "required": true},
			"year": {"type": "integer", "required": false}
		}}
		""";

	@Test
	void validate_필수필드와_타입이_맞으면_유효() {
		var result = validator.validate(SCHEMA, Map.of("employeeId", "E001", "year", 2026));

		assertThat(result.valid()).isTrue();
	}

	@Test
	void validate_옵션필드_생략해도_유효() {
		var result = validator.validate(SCHEMA, Map.of("employeeId", "E001"));

		assertThat(result.valid()).isTrue();
	}

	@Test
	void validate_필수필드_없으면_무효() {
		var result = validator.validate(SCHEMA, Map.of("year", 2026));

		assertThat(result.valid()).isFalse();
		assertThat(result.message()).contains("employeeId");
	}

	@Test
	void validate_타입이_다르면_무효() {
		var result = validator.validate(SCHEMA, Map.of("employeeId", 123));

		assertThat(result.valid()).isFalse();
	}

	@Test
	void validate_스키마에_없는_파라미터는_무효() {
		var result = validator.validate(SCHEMA, Map.of("employeeId", "E001", "extra", "x"));

		assertThat(result.valid()).isFalse();
		assertThat(result.message()).contains("extra");
	}
}
