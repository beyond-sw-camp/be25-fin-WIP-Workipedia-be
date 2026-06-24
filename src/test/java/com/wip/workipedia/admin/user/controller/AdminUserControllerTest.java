package com.wip.workipedia.admin.user.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wip.workipedia.admin.user.dto.AdminUserResponse;
import com.wip.workipedia.admin.user.service.AdminUserService;
import com.wip.workipedia.common.security.InternalApiKeyFilter;
import com.wip.workipedia.common.security.JwtFilter;
import com.wip.workipedia.common.security.JwtProvider;
import com.wip.workipedia.user.domain.UserRole;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
	value = AdminUserController.class,
	excludeAutoConfiguration = {
		SecurityAutoConfiguration.class,
		SecurityFilterAutoConfiguration.class,
		UserDetailsServiceAutoConfiguration.class
	},
	excludeFilters = @ComponentScan.Filter(
		type = FilterType.ASSIGNABLE_TYPE,
		classes = {JwtFilter.class, JwtProvider.class, InternalApiKeyFilter.class}
	)
)
@Import(AdminUserControllerTest.MethodSecurityTestConfig.class)
class AdminUserControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	AdminUserService adminUserService;

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void promoteToTeamAdmin_returnsOkForSystemAdmin() throws Exception {
		AdminUserResponse response = new AdminUserResponse(
			10L, "EMP-10", "사용자", "TEAM_ADMIN", "ACTIVE", 1L, "개발팀", null
		);
		given(adminUserService.promoteToTeamAdmin(nullable(Long.class), eq(10L), eq(UserRole.TEAM_ADMIN)))
			.willReturn(response);

		authenticate(1L, "ROLE_SYSTEM_ADMIN");

		mockMvc.perform(patch("/api/v1/admin/users/10/role")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "role": "TEAM_ADMIN"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.userId").value(10L))
			.andExpect(jsonPath("$.role").value("TEAM_ADMIN"));

		verify(adminUserService).promoteToTeamAdmin(nullable(Long.class), eq(10L), eq(UserRole.TEAM_ADMIN));
	}

	@Test
	void promoteToTeamAdmin_rejectsMissingRole() throws Exception {
		authenticate(1L, "ROLE_SYSTEM_ADMIN");

		mockMvc.perform(patch("/api/v1/admin/users/10/role")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void promoteToTeamAdmin_rejectsInvalidRoleEnum() throws Exception {
		authenticate(1L, "ROLE_SYSTEM_ADMIN");

		mockMvc.perform(patch("/api/v1/admin/users/10/role")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "role": "INVALID_ROLE"
					}
					"""))
			.andExpect(status().isBadRequest());
	}

	@Test
	void promoteToTeamAdmin_rejectsNonSystemAdmin() throws Exception {
		authenticate(2L, "ROLE_USER");

		mockMvc.perform(patch("/api/v1/admin/users/10/role")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "role": "TEAM_ADMIN"
					}
					"""))
			.andExpect(status().isForbidden());
	}

	private void authenticate(Long userId, String role) {
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
			userId,
			null,
			List.of(new SimpleGrantedAuthority(role))
		));
	}

	@TestConfiguration
	@EnableMethodSecurity
	static class MethodSecurityTestConfig {
	}
}
