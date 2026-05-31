package com.wip.workipedia.common.exception;

public class CustomException extends RuntimeException {

	private final ErrorType errorType;

	public CustomException(ErrorType errorType) {
		super(errorType.getMessage());
		this.errorType = errorType;
	}

	public CustomException(ErrorType errorType, String message) {
		super(message);
		this.errorType = errorType;
	}

	public ErrorType getErrorType() {
		return errorType;
	}
}
