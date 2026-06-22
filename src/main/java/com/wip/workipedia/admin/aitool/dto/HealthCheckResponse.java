package com.wip.workipedia.admin.aitool.dto;

public record HealthCheckResponse(boolean success, String toolType, long latencyMs, String errorMessage) {
}
