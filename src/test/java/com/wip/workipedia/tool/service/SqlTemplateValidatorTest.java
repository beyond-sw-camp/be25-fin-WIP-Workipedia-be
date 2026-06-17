package com.wip.workipedia.tool.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class SqlTemplateValidatorTest {

	private final SqlTemplateValidator validator = new SqlTemplateValidator();

	@Test
	void validate_SELECT와_LIMIT가_있으면_유효() {
		var result = validator.validate(
			"SELECT name, remaining_days FROM employee_vacations WHERE employee_id = :employeeId LIMIT 1"
		);

		assertThat(result.valid()).isTrue();
	}

	@Test
	void validate_named_parameter_사용가능() {
		var result = validator.validate("SELECT name FROM employee_vacations WHERE employee_id = :employeeId LIMIT 10");

		assertThat(result.valid()).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"INSERT INTO employee_vacations VALUES (1) LIMIT 1",
		"UPDATE employee_vacations SET remaining_days = 1 LIMIT 1",
		"DELETE FROM employee_vacations LIMIT 1",
		"DROP TABLE employee_vacations",
		"ALTER TABLE employee_vacations ADD COLUMN x INT",
		"TRUNCATE TABLE employee_vacations",
		"CREATE TABLE x (id INT)",
		"MERGE INTO employee_vacations USING dual ON (1=1)",
		"CALL some_procedure()",
		"EXEC some_procedure"
	})
	void validate_금지된_키워드나_SELECT가_아닌_문장은_거부(String sql) {
		assertThat(validator.validate(sql).valid()).isFalse();
	}

	@Test
	void validate_세미콜론_포함시_거부() {
		var result = validator.validate(
			"SELECT name FROM employee_vacations LIMIT 1; DROP TABLE employee_vacations"
		);

		assertThat(result.valid()).isFalse();
	}

	@Test
	void validate_라인주석_포함시_거부() {
		var result = validator.validate("SELECT name FROM employee_vacations LIMIT 1 -- comment");

		assertThat(result.valid()).isFalse();
	}

	@Test
	void validate_블록주석_포함시_거부() {
		var result = validator.validate("SELECT name /* comment */ FROM employee_vacations LIMIT 1");

		assertThat(result.valid()).isFalse();
	}

	@Test
	void validate_LIMIT_없으면_거부() {
		var result = validator.validate("SELECT name FROM employee_vacations WHERE employee_id = :employeeId");

		assertThat(result.valid()).isFalse();
	}
}
