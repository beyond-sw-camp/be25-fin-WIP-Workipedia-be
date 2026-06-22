package com.wip.workipedia.aisync.repository;

import com.wip.workipedia.aisync.domain.AiSyncCleanupLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiSyncCleanupLogRepository extends JpaRepository<AiSyncCleanupLog, Long> {
    List<AiSyncCleanupLog> findByIsDeletedOrderByCompletedAtDesc(String isDeleted, Pageable pageable);
}
