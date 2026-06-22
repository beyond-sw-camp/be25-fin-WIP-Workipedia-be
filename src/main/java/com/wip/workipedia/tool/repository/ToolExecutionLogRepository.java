package com.wip.workipedia.tool.repository;

import com.wip.workipedia.tool.domain.ToolExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ToolExecutionLogRepository extends JpaRepository<ToolExecutionLog, Long> {
}
