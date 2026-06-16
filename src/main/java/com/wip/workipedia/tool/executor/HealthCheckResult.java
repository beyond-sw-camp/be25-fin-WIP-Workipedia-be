package com.wip.workipedia.tool.executor;

public record HealthCheckResult(boolean success, long latencyMs, String errorMessage) {

	public static HealthCheckResult success(long latencyMs) {
		return new HealthCheckResult(true, latencyMs, null);
	}

	public static HealthCheckResult failure(String errorMessage) {
		return new HealthCheckResult(false, 0, errorMessage);
	}

	public static HealthCheckResult failure(long latencyMs, String errorMessage) {
		return new HealthCheckResult(false, latencyMs, errorMessage);
	}
}
