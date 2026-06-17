package com.wip.workipedia.tool.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ParameterSchemaValidator {

	private final ObjectMapper objectMapper;

	public ParameterSchemaValidator(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public ValidationResult validate(String parametersSchemaJson, Map<String, Object> parameters) {
		Map<String, Object> schema;
		try {
			schema = objectMapper.readValue(parametersSchemaJson, new TypeReference<Map<String, Object>>() {});
		} catch (Exception e) {
			return ValidationResult.invalid("Tool 파라미터 스키마가 올바르지 않습니다.");
		}

		Map<String, Object> properties = asMap(schema.get("properties"));
		Map<String, Object> values = parameters == null ? Map.of() : parameters;

		for (String key : values.keySet()) {
			if (!properties.containsKey(key)) {
				return ValidationResult.invalid("허용되지 않은 파라미터입니다: " + key);
			}
		}

		for (Map.Entry<String, Object> entry : properties.entrySet()) {
			String propertyName = entry.getKey();
			Map<String, Object> propertySchema = asMap(entry.getValue());
			boolean required = Boolean.TRUE.equals(propertySchema.get("required"));
			Object value = values.get(propertyName);

			if (value == null) {
				if (required) {
					return ValidationResult.invalid("필수 파라미터가 없습니다: " + propertyName);
				}
				continue;
			}

			String type = (String) propertySchema.get("type");
			if (!matchesType(value, type)) {
				return ValidationResult.invalid("파라미터 타입이 올바르지 않습니다: " + propertyName);
			}
		}

		return ValidationResult.ok();
	}

	private boolean matchesType(Object value, String type) {
		if (type == null) {
			return true;
		}
		return switch (type) {
			case "string" -> value instanceof String;
			case "integer" -> value instanceof Integer || value instanceof Long;
			case "number" -> value instanceof Number;
			case "boolean" -> value instanceof Boolean;
			default -> true;
		};
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> asMap(Object value) {
		return value instanceof Map ? (Map<String, Object>) value : Map.of();
	}

	public record ValidationResult(boolean valid, String message) {
		public static ValidationResult ok() {
			return new ValidationResult(true, null);
		}

		public static ValidationResult invalid(String message) {
			return new ValidationResult(false, message);
		}
	}
}
