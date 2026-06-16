package com.wip.workipedia.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.config.InternalApiProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class InternalApiKeyFilter extends OncePerRequestFilter {

	private static final String INTERNAL_PATH_PREFIX = "/api/v1/internal/";
	private static final String API_KEY_HEADER = "X-Internal-Api-Key";

	private final InternalApiProperties internalApiProperties;
	private final ObjectMapper objectMapper;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		if (!request.getRequestURI().startsWith(INTERNAL_PATH_PREFIX)) {
			filterChain.doFilter(request, response);
			return;
		}

		String apiKey = request.getHeader(API_KEY_HEADER);
		if (apiKey == null || !constantTimeEquals(apiKey, internalApiProperties.apiKey())) {
			writeUnauthorized(response);
			return;
		}

		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
			"ai-server",
			null,
			List.of(new SimpleGrantedAuthority("ROLE_INTERNAL_SERVICE"))
		);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		filterChain.doFilter(request, response);
	}

	// 문자열 비교가 첫 불일치 문자에서 조기 반환되면 응답 시간차로 키를 한 글자씩 추측하는 타이밍 공격에 노출될 수 있어 상수 시간 비교를 사용한다.
	private boolean constantTimeEquals(String apiKey, String expected) {
		if (expected == null) {
			return false;
		}
		return MessageDigest.isEqual(
			apiKey.getBytes(StandardCharsets.UTF_8),
			expected.getBytes(StandardCharsets.UTF_8)
		);
	}

	private void writeUnauthorized(HttpServletResponse response) throws IOException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write(objectMapper.writeValueAsString(
			new ErrorBody(HttpStatus.UNAUTHORIZED.value(), "UNAUTHORIZED", "내부 API 인증에 실패했습니다.", null)
		));
	}

	private record ErrorBody(int code, String status, String message, Object data) {
	}
}
