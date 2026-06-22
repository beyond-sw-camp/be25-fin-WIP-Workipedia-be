package com.wip.workipedia.admin.aisync.service;

import com.wip.workipedia.admin.aisync.dto.AiSyncJobListRequest;
import com.wip.workipedia.admin.aisync.dto.AiSyncJobResponse;
import com.wip.workipedia.admin.aisync.dto.AiSyncJobStatsResponse;
import com.wip.workipedia.aisync.domain.AiSyncJob;
import com.wip.workipedia.aisync.domain.AiSyncStatus;
import com.wip.workipedia.aisync.repository.AiSyncJobRepository;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminAiSyncJobService {

    private final AiSyncJobRepository aiSyncJobRepository;

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
        Map<AiSyncStatus, Long> counts = new EnumMap<>(AiSyncStatus.class);
        aiSyncJobRepository.countByStatusLatest()
            .forEach(row -> counts.put(row.getStatus(), row.getCount()));

        return new AiSyncJobStatsResponse(
            counts.getOrDefault(AiSyncStatus.PENDING, 0L),
            counts.getOrDefault(AiSyncStatus.PROCESSING, 0L),
            counts.getOrDefault(AiSyncStatus.SYNCED, 0L),
            counts.getOrDefault(AiSyncStatus.FAILED, 0L)
        );
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
