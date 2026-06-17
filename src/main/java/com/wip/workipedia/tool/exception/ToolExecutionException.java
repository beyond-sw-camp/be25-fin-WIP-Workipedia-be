package com.wip.workipedia.tool.exception;

public class ToolExecutionException extends RuntimeException {

	private final String errorCode;

	public ToolExecutionException(String errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return errorCode;
	}
}
