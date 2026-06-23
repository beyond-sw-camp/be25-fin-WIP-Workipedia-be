package com.wip.workipedia.common.exception;

import com.wip.workipedia.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

// 공통 예외처리는 여기다 추가하기.
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public Object handleCustomException(CustomException exception) {
		return ApiResponse.error(exception.getErrorType(), exception.getMessage());
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public Object handleMethodNotSupportedException(
		HttpRequestMethodNotSupportedException exception,
		HttpServletRequest request
	) {
		log.warn(
			"[405] method={} uri={} query={} supported={}",
			request.getMethod(),
			request.getRequestURI(),
			request.getQueryString(),
			exception.getSupportedHttpMethods()
		);
		return ApiResponse.error(ErrorType.METHOD_NOT_ALLOWED);
	}

	// 추가 오류 처리 여기다 넣기.
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
		logValidationFailure(exception);
		return exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(this::resolveFieldErrorType)
			.filter(errorType -> errorType != null)
			.findFirst()
			.map(ApiResponse::error)
			.orElseGet(() -> ApiResponse.error(ErrorType.BAD_REQUEST));
	}

	@ExceptionHandler({
		HandlerMethodValidationException.class, // 메서드에 들어갈 파라미터가 잘못된 인수일때,
		ConstraintViolationException.class,
		MissingRequestHeaderException.class, // 헤더가 없을때 바로 예외 발생.
		MissingServletRequestParameterException.class, // 필수 파라미터가 없을때
		MethodArgumentTypeMismatchException.class, // 파라미터 타입이 맞지 않을때
		HttpMessageNotReadableException.class // 요청 본문이 읽을 수 없을때
	})
	// web 예외처리가 없으면, 따로 추가하여 만드는것.
	public Object handleBadRequestException(Exception exception) {
		return ApiResponse.error(ErrorType.BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	public Object handleException(Exception exception) {
		return ApiResponse.error(ErrorType.INTERNAL_ERROR);
	}

	private boolean isInvalidPasswordFormat(FieldError fieldError) {
		String field = fieldError.getField();
		String code = fieldError.getCode();

		return ("password".equals(field) || "newPassword".equals(field))
			&& ("Size".equals(code) || "Pattern".equals(code));
	}

	private ErrorType resolveFieldErrorType(FieldError fieldError) {
		if (isInvalidPasswordFormat(fieldError)) {
			return ErrorType.AUTH_INVALID_PASSWORD_FORMAT;
		}

		if (isInvalidEmailFormat(fieldError)) {
			return ErrorType.AUTH_INVALID_EMAIL_FORMAT;
		}

		if (isInvalidEmailCodeFormat(fieldError)) {
			return ErrorType.AUTH_INVALID_EMAIL_CODE_FORMAT;
		}

		return null;
	}

	private boolean isInvalidEmailFormat(FieldError fieldError) {
		return "email".equals(fieldError.getField())
			&& "Email".equals(fieldError.getCode());
	}

	private boolean isInvalidEmailCodeFormat(FieldError fieldError) {
		return "code".equals(fieldError.getField())
			&& "Pattern".equals(fieldError.getCode());
	}

	private void logValidationFailure(MethodArgumentNotValidException exception) {
		if (!log.isDebugEnabled()) {
			return;
		}

		String fields = exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(fieldError -> fieldError.getField() + ":" + fieldError.getCode())
			.distinct()
			.toList()
			.toString();
		log.debug("Validation failed. fields={}", fields);
	}
}
