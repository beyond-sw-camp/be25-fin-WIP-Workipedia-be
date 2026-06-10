package com.wip.workipedia.common.security;

import com.wip.workipedia.user.domain.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtProvider jwtProvider;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		String authorizationHeader = request.getHeader("Authorization");

		if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
			filterChain.doFilter(request, response);
			return;
		}

		String accessToken = authorizationHeader.substring(BEARER_PREFIX.length());

		if (jwtProvider.isValidAccessToken(accessToken)) {
			Long userId = jwtProvider.getUserId(accessToken);
			UserRole role = jwtProvider.getRole(accessToken);
			SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.name());

			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
				userId,
				null,
				Collections.singleton(authority)
			);

			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

		filterChain.doFilter(request, response);
	}
}

