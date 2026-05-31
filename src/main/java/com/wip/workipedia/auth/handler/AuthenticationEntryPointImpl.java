package com.wip.workipedia.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	public AuthenticationEntryPointImpl(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void commence(
		HttpServletRequest request,
		HttpServletResponse response,
		AuthenticationException authException
	) throws IOException {
		writeErrorResponse(response, ErrorType.UNAUTHORIZED);
	}

	private void writeErrorResponse(
		HttpServletResponse response,
		ErrorType errorType
	) throws IOException {
		response.setStatus(errorType.getHttpStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(
			objectMapper.writeValueAsString(ApiResponse.error(errorType).getBody())
		);
	}
}
