package com.wip.workipedia.admin.repository;

import com.wip.workipedia.admin.domain.AdminLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminLogRepository extends JpaRepository<AdminLog, Long> {
}
