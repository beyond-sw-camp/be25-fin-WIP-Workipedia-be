package com.wip.workipedia.admin.setting.service;

import com.wip.workipedia.admin.setting.dto.AdminSettingsSummaryResponse;
import com.wip.workipedia.manual.repository.ManualRepository;
import com.wip.workipedia.user.domain.UserStatus;
import com.wip.workipedia.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminSettingsService {

	private final UserRepository userRepository;
	private final ManualRepository manualRepository;

	@Transactional(readOnly = true)
	public AdminSettingsSummaryResponse getSummary() {
		LocalDate today = LocalDate.now();
		LocalDateTime startAt = today.atStartOfDay();
		LocalDateTime endAt = today.plusDays(1).atStartOfDay();

		return new AdminSettingsSummaryResponse(
			userRepository.countByStatus(UserStatus.ACTIVE),
			userRepository.countByStatusAndLastLoginAtGreaterThanEqualAndLastLoginAtLessThan(
				UserStatus.ACTIVE,
				startAt,
				endAt
			),
			manualRepository.countByDeletedAtIsNull()
		);
	}
}
