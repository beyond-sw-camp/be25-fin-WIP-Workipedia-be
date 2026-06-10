package com.wip.workipedia.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	@Override
	public void handle(
		HttpServletRequest request,
		HttpServletResponse response,
		AccessDeniedException accessDeniedException
	) throws IOException {
		writeErrorResponse(response, ErrorType.FORBIDDEN);
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
