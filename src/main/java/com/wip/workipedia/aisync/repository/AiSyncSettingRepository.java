package com.wip.workipedia.aisync.repository;

import com.wip.workipedia.aisync.domain.AiSyncSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiSyncSettingRepository extends JpaRepository<AiSyncSetting, Long> {
    Optional<AiSyncSetting> findFirstByIsDeleted(String isDeleted);
}
