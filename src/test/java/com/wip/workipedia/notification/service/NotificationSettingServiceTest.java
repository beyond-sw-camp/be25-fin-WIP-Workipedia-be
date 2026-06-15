package com.wip.workipedia.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wip.workipedia.mypage.dto.MyPageResponse;
import com.wip.workipedia.notification.domain.NotificationSetting;
import com.wip.workipedia.notification.dto.NotificationSettingRequest;
import com.wip.workipedia.notification.repository.NotificationSettingRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationSettingServiceTest {

	@Mock
	private NotificationSettingRepository notificationSettingRepository;

	@InjectMocks
	private NotificationSettingService notificationSettingService;

	@Test
	@DisplayName("전체 알림을 켜면 하위 알림 설정도 모두 켜진다")
	void update_allEnabledTrue_enablesAllNotificationSettings() {
		Long userId = 1L;
		NotificationSetting notificationSetting = NotificationSetting.createDefault(userId);
		notificationSetting.update(false, false, false);
		when(notificationSettingRepository.findByUserIdAndDeletedAtIsNull(userId))
			.thenReturn(Optional.of(notificationSetting));

		MyPageResponse.NotificationSettings response = notificationSettingService.update(
			userId,
			new NotificationSettingRequest(true, false, false, false)
		);

		ArgumentCaptor<NotificationSetting> captor = ArgumentCaptor.forClass(NotificationSetting.class);
		verify(notificationSettingRepository).save(captor.capture());
		NotificationSetting saved = captor.getValue();

		assertThat(response.allEnabled()).isTrue();
		assertThat(response.ticketEnabled()).isTrue();
		assertThat(response.workiEnabled()).isTrue();
		assertThat(response.manualEnabled()).isTrue();
		assertThat(saved.isAllEnabled()).isTrue();
		assertThat(saved.isTicketEnabled()).isTrue();
		assertThat(saved.isWorkiEnabled()).isTrue();
		assertThat(saved.isManualEnabled()).isTrue();
	}

	@Test
	@DisplayName("전체 알림을 끄고 하위 알림도 모두 끄면 모든 알림 설정이 꺼진다")
	void update_allEnabledFalse_disablesAllNotificationSettings() {
		Long userId = 1L;
		NotificationSetting notificationSetting = NotificationSetting.createDefault(userId);
		when(notificationSettingRepository.findByUserIdAndDeletedAtIsNull(userId))
			.thenReturn(Optional.of(notificationSetting));

		MyPageResponse.NotificationSettings response = notificationSettingService.update(
			userId,
			new NotificationSettingRequest(false, false, false, false)
		);

		ArgumentCaptor<NotificationSetting> captor = ArgumentCaptor.forClass(NotificationSetting.class);
		verify(notificationSettingRepository).save(captor.capture());
		NotificationSetting saved = captor.getValue();

		assertThat(response.allEnabled()).isFalse();
		assertThat(response.ticketEnabled()).isFalse();
		assertThat(response.workiEnabled()).isFalse();
		assertThat(response.manualEnabled()).isFalse();
		assertThat(saved.isAllEnabled()).isFalse();
		assertThat(saved.isTicketEnabled()).isFalse();
		assertThat(saved.isWorkiEnabled()).isFalse();
		assertThat(saved.isManualEnabled()).isFalse();
	}

	@Test
	@DisplayName("전체 알림이 꺼진 상태에서는 하위 알림 설정을 개별 저장할 수 있다")
	void update_allEnabledFalse_savesDetailNotificationSettings() {
		Long userId = 1L;
		when(notificationSettingRepository.findByUserIdAndDeletedAtIsNull(userId))
			.thenReturn(Optional.empty());

		MyPageResponse.NotificationSettings response = notificationSettingService.update(
			userId,
			new NotificationSettingRequest(false, true, false, true)
		);

		ArgumentCaptor<NotificationSetting> captor = ArgumentCaptor.forClass(NotificationSetting.class);
		verify(notificationSettingRepository).save(captor.capture());
		NotificationSetting saved = captor.getValue();

		assertThat(response.allEnabled()).isFalse();
		assertThat(response.ticketEnabled()).isTrue();
		assertThat(response.workiEnabled()).isFalse();
		assertThat(response.manualEnabled()).isTrue();
		assertThat(saved.isAllEnabled()).isFalse();
		assertThat(saved.isTicketEnabled()).isTrue();
		assertThat(saved.isWorkiEnabled()).isFalse();
		assertThat(saved.isManualEnabled()).isTrue();
	}
}
