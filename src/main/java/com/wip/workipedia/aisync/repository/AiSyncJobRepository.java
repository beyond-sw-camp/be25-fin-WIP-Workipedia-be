package com.wip.workipedia.aisync.repository;

import com.wip.workipedia.aisync.domain.AiSyncJob;
import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.aisync.domain.AiSyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AiSyncJobRepository extends JpaRepository<AiSyncJob, Long> {

    // MANUAL 전용 (PDF 포함) — document-delay-ms 주기로 처리
    @Modifying
    @Query(
        value = """
            SELECT * FROM ai_sync_jobs
            WHERE status = 'PENDING'
              AND source_type = 'MANUAL'
              AND (next_retry_at IS NULL OR next_retry_at <= :now)
              AND deleted_at IS NULL
            ORDER BY created_at ASC
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """,
        nativeQuery = true
    )
    List<AiSyncJob> claimPendingDocumentJobs(
        @Param("now") LocalDateTime now,
        @Param("limit") int limit
    );

    // 텍스트 계열 (WORKI, KNOWLEDGE_DATA, MANUAL_KNOWLEDGE, DEPT_RR) — text-delay-ms 주기로 처리
    @Modifying
    @Query(
        value = """
            SELECT * FROM ai_sync_jobs
            WHERE status = 'PENDING'
              AND source_type IN ('WORKI', 'KNOWLEDGE_DATA', 'MANUAL_KNOWLEDGE', 'DEPT_RR')
              AND (next_retry_at IS NULL OR next_retry_at <= :now)
              AND deleted_at IS NULL
            ORDER BY created_at ASC
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """,
        nativeQuery = true
    )
    List<AiSyncJob> claimPendingTextJobs(
        @Param("now") LocalDateTime now,
        @Param("limit") int limit
    );

    @Query("""
        SELECT j FROM AiSyncJob j
        WHERE j.status = com.wip.workipedia.aisync.domain.AiSyncStatus.PROCESSING
          AND j.leaseExpiresAt < :now
          AND j.deletedAt IS NULL
        """)
    List<AiSyncJob> findLeaseExpiredJobs(@Param("now") LocalDateTime now);

    // created_at 이후 더 최신 SYNCED job이 있으면 이 job은 AI 호출 없이 스킵
    @Query("""
        SELECT COUNT(j) > 0 FROM AiSyncJob j
        WHERE j.sourceType = :sourceType
          AND j.sourceId = :sourceId
          AND j.status = com.wip.workipedia.aisync.domain.AiSyncStatus.SYNCED
          AND j.createdAt > :createdAt
          AND j.deletedAt IS NULL
        """)
    boolean hasNewerSyncedJob(
        @Param("sourceType") AiSyncSourceType sourceType,
        @Param("sourceId") Long sourceId,
        @Param("createdAt") LocalDateTime createdAt
    );
}
