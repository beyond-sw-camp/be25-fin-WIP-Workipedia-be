package com.wip.workipedia.common.response;

import com.wip.workipedia.common.exception.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public record ApiResponse<T>(
	int code,
	String status,
	String message,
	T data
) {

	public static <T> ResponseEntity<ApiResponse<T>> success(
		HttpStatus httpStatus,
		String message,
		T data
	) {
		ApiResponse<T> body = new ApiResponse<>(
			httpStatus.value(),
			httpStatus.name(),
			message,
			data
		);

		return ResponseEntity.status(httpStatus).body(body);
	}

	public static ResponseEntity<ApiResponse<Void>> success(
		HttpStatus httpStatus,
		String message
	) {
		return success(httpStatus, message, null);
	}

	public static ResponseEntity<ApiResponse<Void>> error(ErrorType errorType) {
		return error(errorType, errorType.getMessage());
	}

	public static ResponseEntity<ApiResponse<Void>> error(
		ErrorType errorType,
		String message
	) {
		HttpStatus httpStatus = errorType.getHttpStatus();
		ApiResponse<Void> body = new ApiResponse<>(
			httpStatus.value(),
			errorType.getStatus(),
			message,
			null
		);

		return ResponseEntity.status(httpStatus).body(body);
	}
}
