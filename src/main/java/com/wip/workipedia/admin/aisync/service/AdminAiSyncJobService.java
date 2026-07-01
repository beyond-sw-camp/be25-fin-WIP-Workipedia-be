package com.wip.workipedia.admin.aisync.service;

import com.wip.workipedia.admin.aisync.dto.AiSyncJobListRequest;
import com.wip.workipedia.admin.aisync.dto.AiSyncJobResponse;
import com.wip.workipedia.admin.aisync.dto.AiSyncJobStatsResponse;
import com.wip.workipedia.admin.aisync.dto.KnowledgeSyncRequest;
import com.wip.workipedia.aisync.domain.AiSyncJob;
import com.wip.workipedia.aisync.domain.AiSyncOperation;
import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.aisync.domain.AiSyncStatus;
import com.wip.workipedia.aisync.repository.AiSyncJobRepository;
import com.wip.workipedia.aisync.service.AiSyncJobService;
import com.wip.workipedia.aisync.worker.AiSyncKnowledgeRunner;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.directdata.repository.DirectDataRepository;
import com.wip.workipedia.knowledge.repository.KnowledgeDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminAiSyncJobService {

    private final AiSyncJobRepository aiSyncJobRepository;
    private final AiSyncJobService aiSyncJobService;
    private final AiSyncKnowledgeRunner knowledgeRunner;
    private final KnowledgeDataRepository knowledgeDataRepository;
    private final DirectDataRepository directDataRepository;

    @Transactional(readOnly = true)
    public PageResponse<AiSyncJobResponse> getJobs(AiSyncJobListRequest req) {
        String status = req.getStatus() != null ? req.getStatus().name() : null;
        String sourceType = req.getSourceType() != null ? req.getSourceType().name() : null;

        Page<AiSyncJob> page = aiSyncJobRepository.findLatestJobsPerSource(
            status,
            sourceType,
            req.getFrom(),
            req.getTo(),
            req.toPageable(Sort.unsorted())
        );
        return PageResponse.from(page.map(AiSyncJobResponse::from));
    }

    @Transactional(readOnly = true)
    public AiSyncJobStatsResponse getStats() {
        return getStats(null);
    }

    // sourceTypes가 null/empty면 전체 집계, 아니면 해당 스코프만 집계
    @Transactional(readOnly = true)
    public AiSyncJobStatsResponse getStats(List<AiSyncSourceType> sourceTypes) {
        Map<AiSyncStatus, Long> counts = new EnumMap<>(AiSyncStatus.class);
        List<AiSyncJobRepository.AiSyncStatusCount> rows =
            (sourceTypes == null || sourceTypes.isEmpty())
                ? aiSyncJobRepository.countByStatusLatest()
                : aiSyncJobRepository.countByStatusLatestForSourceTypes(
                    sourceTypes.stream().map(Enum::name).toList());
        rows.forEach(row -> counts.put(row.getStatus(), row.getCount()));

        return new AiSyncJobStatsResponse(
            counts.getOrDefault(AiSyncStatus.PENDING, 0L),
            counts.getOrDefault(AiSyncStatus.PROCESSING, 0L),
            counts.getOrDefault(AiSyncStatus.SYNCED, 0L),
            counts.getOrDefault(AiSyncStatus.FAILED, 0L)
        );
    }

    // 지식 데이터 즉시 실행 — 스코프 PENDING 건수를 queued로 반환하고 비동기 드레인 트리거
    public Map<String, Long> runNowKnowledge(KnowledgeSyncRequest req) {
        List<AiSyncSourceType> scope = req.normalized();
        long queued = aiSyncJobRepository.countPendingBySourceTypes(
            scope.stream().map(Enum::name).toList());
        knowledgeRunner.drain(scope);
        return Map.of("queued", queued);
    }

    // 지식 데이터 전체 재동기화 — 활성 원본 전체에 UPSERT 잡 생성(중복은 skip)
    @Transactional
    public Map<String, Integer> resyncKnowledge(KnowledgeSyncRequest req) {
        List<AiSyncSourceType> scope = req.normalized();
        int enqueued = 0;
        int skipped = 0;
        for (AiSyncSourceType type : scope) {
            List<Long> ids = switch (type) {
                case KNOWLEDGE_DATA -> knowledgeDataRepository.findActiveIds();
                case MANUAL_KNOWLEDGE -> directDataRepository.findActiveIds();
                default -> throw new CustomException(ErrorType.BAD_REQUEST);
            };
            for (Long id : ids) {
                if (aiSyncJobService.enqueueIfAbsent(type, id, AiSyncOperation.UPSERT)) {
                    enqueued++;
                } else {
                    skipped++;
                }
            }
        }
        return Map.of("enqueued", enqueued, "skipped", skipped);
    }

    @Transactional
    public void retryJob(Long jobId) {
        AiSyncJob job = aiSyncJobRepository.findByAiSyncJobIdAndDeletedAtIsNull(jobId)
            .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND));

        if (job.getStatus() != AiSyncStatus.FAILED) {
            throw new CustomException(ErrorType.CONFLICT);
        }
        job.resetFailedForManualRetry();
    }

    @Transactional
    public int retryAllFailed() {
        return aiSyncJobRepository.resetAllFailed();
    }
}
