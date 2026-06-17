package com.wip.workipedia.tool.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tool_execution_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ToolExecutionLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "tool_execution_log_id")
	private Long toolExecutionLogId;

	@Column(name = "ai_tool_id", nullable = false)
	private Long aiToolId;

	@Column(nullable = false, length = 100)
	private String caller;

	@Column(name = "masked_parameters", columnDefinition = "JSON")
	private String maskedParameters;

	private Integer resultCount;

	@Column(nullable = false)
	private long durationMs;

	@Column(nullable = false, columnDefinition = "CHAR(1)")
	private String success;

	@Column(length = 50)
	private String errorCode;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	public static ToolExecutionLog of(
		Long aiToolId, String caller, String maskedParametersJson,
		Integer resultCount, long durationMs, boolean success, String errorCode
	) {
		ToolExecutionLog log = new ToolExecutionLog();
		log.aiToolId = aiToolId;
		log.caller = caller;
		log.maskedParameters = maskedParametersJson;
		log.resultCount = resultCount;
		log.durationMs = durationMs;
		log.success = success ? "Y" : "N";
		log.errorCode = errorCode;
		log.createdAt = LocalDateTime.now();
		return log;
	}
}
