package com.wip.workipedia.common.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.wip.workipedia.common.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

	@Test
	void validationErrorResponseDoesNotExposeRejectedPassword() {
		GlobalExceptionHandler handler = new GlobalExceptionHandler();
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "signupRequest");
		bindingResult.addError(new FieldError(
			"signupRequest",
			"password",
			"Test1234!",
			false,
			new String[] {"Pattern"},
			null,
			"비밀번호 형식이 올바르지 않습니다."
		));
		MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
		when(exception.getBindingResult()).thenReturn(bindingResult);

		Object response = handler.handleMethodArgumentNotValidException(exception);

		assertThat(response).isInstanceOf(ResponseEntity.class);
		Object body = ((ResponseEntity<?>) response).getBody();
		assertThat(body).isInstanceOf(ApiResponse.class);
		assertThat(body.toString()).doesNotContain("Test1234!");
		assertThat(((ApiResponse<?>) body).status()).isEqualTo(ErrorType.AUTH_INVALID_PASSWORD_FORMAT.getStatus());
	}
}
