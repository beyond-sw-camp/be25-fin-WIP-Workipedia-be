package com.wip.workipedia.notification.repository;

import com.wip.workipedia.notification.domain.NotificationSetting;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

	Optional<NotificationSetting> findByUserIdAndDeletedAtIsNull(Long userId);
}
