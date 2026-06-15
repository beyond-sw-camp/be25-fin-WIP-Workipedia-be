package com.wip.workipedia.notification.service;

import com.wip.workipedia.common.domain.ModifiedSource;
import com.wip.workipedia.mypage.dto.MyPageResponse;
import com.wip.workipedia.notification.domain.NotificationSetting;
import com.wip.workipedia.notification.dto.NotificationSettingRequest;
import com.wip.workipedia.notification.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationSettingService {

	private final NotificationSettingRepository notificationSettingRepository;

	// 사용자의 알림 설정을 변경하고, 변경된 설정 상태를 응답으로 반환합니다.
	@Transactional
	public MyPageResponse.NotificationSettings update(
		Long userId,
		NotificationSettingRequest request
	) {
		NotificationSetting notificationSetting = notificationSettingRepository.findByUserIdAndDeletedAtIsNull(userId)
			.orElseGet(() -> NotificationSetting.createDefault(userId));

		boolean ticketEnabled = request.allEnabled() || request.ticketEnabled();
		boolean workiEnabled = request.allEnabled() || request.workiEnabled();
		boolean manualEnabled = request.allEnabled() || request.manualEnabled();

		notificationSetting.update(ticketEnabled, workiEnabled, manualEnabled);
		notificationSetting.touchModifiedSource(ModifiedSource.USER);
		notificationSettingRepository.save(notificationSetting);

		return toResponse(notificationSetting);
	}

	// 알림 설정 엔티티를 마이페이지 응답에서 사용하는 알림 설정 DTO로 변환합니다.
	private MyPageResponse.NotificationSettings toResponse(NotificationSetting notificationSetting) {
		return new MyPageResponse.NotificationSettings(
			notificationSetting.isAllEnabled(),
			notificationSetting.isTicketEnabled(),
			notificationSetting.isWorkiEnabled(),
			notificationSetting.isManualEnabled()
		);
	}
}
