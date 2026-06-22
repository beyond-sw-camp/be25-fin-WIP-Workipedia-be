package com.wip.workipedia.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.config.InternalApiProperties;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class InternalApiKeyFilterTest {

	private final InternalApiProperties properties = new InternalApiProperties("correct-key");
	private final InternalApiKeyFilter filter = new InternalApiKeyFilter(properties, new ObjectMapper());

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void doFilterInternal_internal경로가_아니면_그대로_통과() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/admin/ai-tools");
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		filter.doFilterInternal(request, response, chain);

		verify(chain).doFilter(request, response);
	}

	@Test
	void doFilterInternal_올바른_키면_통과하고_인증정보_설정() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/internal/ai-tools/active");
		request.addHeader("X-Internal-Api-Key", "correct-key");
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		filter.doFilterInternal(request, response, chain);

		verify(chain).doFilter(request, response);
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
	}

	@Test
	void doFilterInternal_키가_틀리면_401이고_체인_호출안함() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/internal/ai-tools/active");
		request.addHeader("X-Internal-Api-Key", "wrong-key");
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		filter.doFilterInternal(request, response, chain);

		assertThat(response.getStatus()).isEqualTo(401);
		verifyNoInteractions(chain);
	}
}
