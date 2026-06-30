package com.wip.workipedia.aisync.repository;

import com.wip.workipedia.aisync.domain.AiSyncJob;
import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.aisync.domain.AiSyncStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AiSyncJobRepository extends JpaRepository<AiSyncJob, Long> {

    // MANUAL 전용 (PDF 포함) — document-delay-ms 주기로 처리
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

    @Query(
        value = """
            SELECT
              ai_sync_job_id AS aiSyncJobId,
              source_type AS sourceType,
              source_id AS sourceId,
              operation AS operation,
              status AS status,
              retry_count AS retryCount,
              next_retry_at AS nextRetryAt,
              lease_expires_at AS leaseExpiresAt,
              created_at AS createdAt,
              updated_at AS updatedAt
            FROM ai_sync_jobs
            WHERE status = 'PENDING'
              AND source_type = 'MANUAL'
              AND (next_retry_at IS NULL OR next_retry_at <= :now)
              AND deleted_at IS NULL
            ORDER BY created_at ASC
            LIMIT :limit
            """,
        nativeQuery = true
    )
    List<AiSyncClaimCandidate> findDocumentClaimCandidates(
        @Param("now") LocalDateTime now,
        @Param("limit") int limit
    );

    // 텍스트 계열 (WORKI, KNOWLEDGE_DATA, MANUAL_KNOWLEDGE, DEPT_RR, MANUAL_CHANGE_SUMMARY) — text-delay-ms 주기로 처리
    @Query(
        value = """
            SELECT * FROM ai_sync_jobs
            WHERE status = 'PENDING'
              AND source_type IN ('WORKI', 'KNOWLEDGE_DATA', 'MANUAL_KNOWLEDGE', 'DEPT_RR', 'MANUAL_CHANGE_SUMMARY')
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

    @Query(
        value = """
            SELECT
              ai_sync_job_id AS aiSyncJobId,
              source_type AS sourceType,
              source_id AS sourceId,
              operation AS operation,
              status AS status,
              retry_count AS retryCount,
              next_retry_at AS nextRetryAt,
              lease_expires_at AS leaseExpiresAt,
              created_at AS createdAt,
              updated_at AS updatedAt
            FROM ai_sync_jobs
            WHERE status = 'PENDING'
              AND source_type IN ('WORKI', 'KNOWLEDGE_DATA', 'MANUAL_KNOWLEDGE', 'DEPT_RR', 'MANUAL_CHANGE_SUMMARY')
              AND (next_retry_at IS NULL OR next_retry_at <= :now)
              AND deleted_at IS NULL
            ORDER BY created_at ASC
            LIMIT :limit
            """,
        nativeQuery = true
    )
    List<AiSyncClaimCandidate> findTextClaimCandidates(
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

    // (sourceType, sourceId)별 최신 job만 골라 목록 조회 — MAX(ai_sync_job_id) 기준으로 동시성 문제 방지
    @Query(value = """
        SELECT j.* FROM ai_sync_jobs j
        INNER JOIN (
            SELECT source_type, source_id, MAX(ai_sync_job_id) AS max_id
            FROM ai_sync_jobs
            WHERE deleted_at IS NULL
            GROUP BY source_type, source_id
        ) latest ON j.ai_sync_job_id = latest.max_id
        WHERE j.deleted_at IS NULL
          AND (:status IS NULL OR j.status = :status)
          AND (:sourceType IS NULL OR j.source_type = :sourceType)
          AND (:from IS NULL OR j.created_at >= :from)
          AND (:to IS NULL OR j.created_at <= :to)
        ORDER BY j.ai_sync_job_id DESC
        """,
        countQuery = """
        SELECT COUNT(*) FROM ai_sync_jobs j
        INNER JOIN (
            SELECT source_type, source_id, MAX(ai_sync_job_id) AS max_id
            FROM ai_sync_jobs
            WHERE deleted_at IS NULL
            GROUP BY source_type, source_id
        ) latest ON j.ai_sync_job_id = latest.max_id
        WHERE j.deleted_at IS NULL
          AND (:status IS NULL OR j.status = :status)
          AND (:sourceType IS NULL OR j.source_type = :sourceType)
          AND (:from IS NULL OR j.created_at >= :from)
          AND (:to IS NULL OR j.created_at <= :to)
        """,
        nativeQuery = true)
    Page<AiSyncJob> findLatestJobsPerSource(
        @Param("status") String status,
        @Param("sourceType") String sourceType,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to,
        Pageable pageable
    );

    @Query(value = """
        SELECT j.* FROM ai_sync_jobs j
        INNER JOIN (
            SELECT source_id, MAX(ai_sync_job_id) AS max_id
            FROM ai_sync_jobs
            WHERE source_type = :sourceType
              AND source_id IN (:sourceIds)
              AND deleted_at IS NULL
            GROUP BY source_id
        ) latest ON j.ai_sync_job_id = latest.max_id
        WHERE j.deleted_at IS NULL
        """, nativeQuery = true)
    List<AiSyncJob> findLatestJobsBySourceTypeAndSourceIds(
        @Param("sourceType") String sourceType,
        @Param("sourceIds") List<Long> sourceIds
    );

    // 상태별 카운트 — 각 source의 최신 job 기준
    @Query(value = """
        SELECT j.status AS status, COUNT(*) AS count
        FROM ai_sync_jobs j
        INNER JOIN (
            SELECT source_type, source_id, MAX(ai_sync_job_id) AS max_id
            FROM ai_sync_jobs
            WHERE deleted_at IS NULL
            GROUP BY source_type, source_id
        ) latest ON j.ai_sync_job_id = latest.max_id
        WHERE j.deleted_at IS NULL
        GROUP BY j.status
        """, nativeQuery = true)
    List<AiSyncStatusCount> countByStatusLatest();

    // 전체 FAILED 잡 PENDING 리셋
    @Modifying
    @Query("""
        UPDATE AiSyncJob j
        SET j.status = com.wip.workipedia.aisync.domain.AiSyncStatus.PENDING,
            j.retryCount = 0,
            j.leaseExpiresAt = NULL,
            j.nextRetryAt = NULL,
            j.lastError = NULL,
            j.startedAt = NULL,
            j.completedAt = NULL
        WHERE j.status = com.wip.workipedia.aisync.domain.AiSyncStatus.FAILED
          AND j.deletedAt IS NULL
        """)
    int resetAllFailed();

    Optional<AiSyncJob> findByAiSyncJobIdAndDeletedAtIsNull(Long aiSyncJobId);

    /**
     * sourceId별 최신 WORKI 잡이 SYNCED이고 completed_at이 cutoff보다 오래된 것 조회.
     */
    @Query(value = """
        SELECT j.* FROM ai_sync_jobs j
        INNER JOIN (
            SELECT source_id, MAX(ai_sync_job_id) AS max_id
            FROM ai_sync_jobs
            WHERE source_type = 'WORKI'
              AND deleted_at IS NULL
            GROUP BY source_id
        ) latest ON j.ai_sync_job_id = latest.max_id
        WHERE j.status = 'SYNCED'
          AND j.completed_at < :cutoff
          AND j.deleted_at IS NULL
        ORDER BY j.ai_sync_job_id ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<AiSyncJob> findOldSyncedWorkiLatestJobs(
        @Param("cutoff") LocalDateTime cutoff,
        @Param("limit") int limit
    );

    /**
     * aiSyncJobId <= maxJobId 범위만 soft delete — 조회 이후 생긴 신규 잡은 건드리지 않는다.
     */
    @Transactional
    @Modifying
    @Query("""
        UPDATE AiSyncJob j
        SET j.deletedAt = :now
        WHERE j.sourceType = :sourceType
          AND j.sourceId = :sourceId
          AND j.aiSyncJobId <= :maxJobId
          AND j.deletedAt IS NULL
        """)
    int softDeleteOldJobsBySourceId(
        @Param("sourceType") AiSyncSourceType sourceType,
        @Param("sourceId") Long sourceId,
        @Param("maxJobId") Long maxJobId,
        @Param("now") LocalDateTime now
    );


    interface AiSyncStatusCount {
        AiSyncStatus getStatus();
        long getCount();
    }

    interface AiSyncClaimCandidate {
        Long getAiSyncJobId();
        String getSourceType();
        Long getSourceId();
        String getOperation();
        String getStatus();
        Integer getRetryCount();
        LocalDateTime getNextRetryAt();
        LocalDateTime getLeaseExpiresAt();
        LocalDateTime getCreatedAt();
        LocalDateTime getUpdatedAt();
    }
}
