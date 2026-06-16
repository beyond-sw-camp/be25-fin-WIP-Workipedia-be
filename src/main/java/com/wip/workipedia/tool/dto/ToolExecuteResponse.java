package com.wip.workipedia.tool.dto;

public record ToolExecuteResponse(Object data, String errorCode, String errorMessage) {

	public static ToolExecuteResponse success(Object data) {
		return new ToolExecuteResponse(data, null, null);
	}

	public static ToolExecuteResponse failure(String errorCode, String errorMessage) {
		return new ToolExecuteResponse(null, errorCode, errorMessage);
	}
}
