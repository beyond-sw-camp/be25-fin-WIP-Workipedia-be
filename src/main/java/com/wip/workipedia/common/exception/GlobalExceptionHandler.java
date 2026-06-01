package com.wip.workipedia.common.exception;

import com.wip.workipedia.common.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public Object handleCustomException(CustomException exception) {
		return ApiResponse.error(exception.getErrorType(), exception.getMessage());
	}

	@ExceptionHandler({
		MethodArgumentNotValidException.class,
		HandlerMethodValidationException.class,
		ConstraintViolationException.class,
		HttpMessageNotReadableException.class
	})
	public Object handleBadRequestException(Exception exception) {
		return ApiResponse.error(ErrorType.BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	public Object handleException(Exception exception) {
		return ApiResponse.error(ErrorType.INTERNAL_ERROR);
	}
}
