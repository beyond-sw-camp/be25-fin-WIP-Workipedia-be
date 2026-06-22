package com.wip.workipedia.config;

import com.wip.workipedia.auth.handler.AccessDeniedHandlerImpl;
import com.wip.workipedia.auth.handler.AuthenticationEntryPointImpl;
import com.wip.workipedia.common.security.InternalApiKeyFilter;
import com.wip.workipedia.common.security.JwtFilter;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final AuthenticationEntryPointImpl authenticationEntryPoint;
	private final AccessDeniedHandlerImpl accessDeniedHandler;
	private final JwtFilter jwtFilter;
	private final InternalApiKeyFilter internalApiKeyFilter;

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(List.of("*"));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setExposedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
					.exceptionHandling(exception -> exception
							.authenticationEntryPoint(authenticationEntryPoint)
							.accessDeniedHandler(accessDeniedHandler))
					.authorizeHttpRequests(auth -> auth
							// SSE 스트리밍(Flux 반환)은 서블릿 async로 처리되는데, 작업 완료 후 ASYNC dispatch로 필터 체인을 다시 탄다.
							// STATELESS(JWT)라 이 재진입 시점엔 SecurityContext가 비어 있어 AuthorizationFilter가 거부하고,
							// 이미 응답이 커밋된 뒤라 "response already committed" 에러로 이어진다. 첫 REQUEST에서 이미 인가했으므로
							// 우리가 시작한 스트림의 마무리인 ASYNC 재진입은 재검사에서 제외한다.
							.dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
							.requestMatchers("/error").permitAll()
							.requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/departments").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/signup/code").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/signup/code/verify").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/signup").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/token/refresh").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/password-reset/code").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/password-reset/code/verify").permitAll()
						.requestMatchers(HttpMethod.PATCH, "/api/v1/auth/password-reset").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/flash-chat/**").permitAll()
						.requestMatchers("/ws/flash-chat/**").permitAll()
						.requestMatchers("/ws/flash-chat-native/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/search/**").permitAll()
							.requestMatchers("/api/v1/admin/team/**").hasAnyRole("TEAM_ADMIN", "SYSTEM_ADMIN")
						.requestMatchers("/api/v1/admin/**").hasRole("SYSTEM_ADMIN")
						.requestMatchers("/api/v1/faq/**").permitAll()
						.requestMatchers("/api/v1/internal/**").permitAll()
						.anyRequest().authenticated())
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(internalApiKeyFilter, JwtFilter.class)
				.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
