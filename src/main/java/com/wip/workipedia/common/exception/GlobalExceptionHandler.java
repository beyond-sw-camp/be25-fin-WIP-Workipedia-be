package com.wip.workipedia.common.exception;

import com.wip.workipedia.common.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

// 공통 예외처리는 여기다 추가하기.
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public Object handleCustomException(CustomException exception) {
		return ApiResponse.error(exception.getErrorType(), exception.getMessage());
	}

	// 추가 오류 처리 여기다 넣기.
	@ExceptionHandler({
		MethodArgumentNotValidException.class, // 유효성 검사 실패시. 
		HandlerMethodValidationException.class, // 메서드에 들어갈 파라미터가 잘못된 인수일때,
		ConstraintViolationException.class, // 
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
}
