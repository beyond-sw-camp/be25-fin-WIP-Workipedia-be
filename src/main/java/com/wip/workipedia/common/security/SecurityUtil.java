package com.wip.workipedia.common.security;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {
	private SecurityUtil() {
	}

	public static Long getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new CustomException(ErrorType.UNAUTHORIZED);
		}

		Object principal = authentication.getPrincipal();
		if (principal instanceof Long userId) {
			return userId;
		}
		if (principal instanceof String userId) {
			try {
				return Long.parseLong(userId);
			} catch (NumberFormatException exception) {
				throw new CustomException(ErrorType.UNAUTHORIZED);
			}
		}

		throw new CustomException(ErrorType.UNAUTHORIZED);
	}
}
