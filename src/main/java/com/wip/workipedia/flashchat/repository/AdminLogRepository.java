package com.wip.workipedia.flashchat.repository;

import com.wip.workipedia.flashchat.domain.AdminLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminLogRepository extends JpaRepository<AdminLog, Long> {
}
