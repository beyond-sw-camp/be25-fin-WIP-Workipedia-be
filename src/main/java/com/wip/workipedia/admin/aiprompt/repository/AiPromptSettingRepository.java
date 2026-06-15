package com.wip.workipedia.admin.aiprompt.repository;

import com.wip.workipedia.admin.aiprompt.domain.AiPromptSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiPromptSettingRepository extends JpaRepository<AiPromptSetting, Long> {
}
