package com.wip.workipedia.aisync.service;

import com.wip.workipedia.admin.aisync.dto.AiSyncCleanupLogResponse;
import com.wip.workipedia.admin.aisync.dto.AiSyncCleanupResponse;
import com.wip.workipedia.admin.aisync.dto.AiSyncSettingResponse;
import com.wip.workipedia.admin.aisync.dto.AiSyncSettingUpdateRequest;
import com.wip.workipedia.aisync.client.TextDocumentAiClient;
import com.wip.workipedia.aisync.domain.AiSyncCleanupLog;
import com.wip.workipedia.aisync.domain.AiSyncJob;
import com.wip.workipedia.aisync.domain.AiSyncSetting;
import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.aisync.domain.CleanupTrigger;
import com.wip.workipedia.aisync.repository.AiSyncCleanupLogRepository;
import com.wip.workipedia.aisync.repository.AiSyncJobRepository;
import com.wip.workipedia.aisync.repository.AiSyncSettingRepository;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.config.AiSyncProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSyncCleanupService {

    private final AiSyncJobRepository aiSyncJobRepository;
    private final AiSyncSettingRepository aiSyncSettingRepository;
    private final AiSyncCleanupLogRepository aiSyncCleanupLogRepository;
    private final TextDocumentAiClient textDocumentAiClient;
    private final AiSyncProperties aiSyncProperties;

    @Transactional(readOnly = true)
    public AiSyncSettingResponse getSetting() {
        return AiSyncSettingResponse.from(loadSetting());
    }

    @Transactional
    public AiSyncSettingResponse updateSetting(AiSyncSettingUpdateRequest req) {
        AiSyncSetting setting = loadSetting();
        setting.updateRetentionDays(req.retentionDays());
        return AiSyncSettingResponse.from(setting);
    }

    @Transactional(readOnly = true)
    public List<AiSyncCleanupLogResponse> getRecentLogs(int limit) {
        return aiSyncCleanupLogRepository
            .findByIsDeletedOrderByCompletedAtDesc("N", PageRequest.of(0, limit))
            .stream()
            .map(AiSyncCleanupLogResponse::from)
            .toList();
    }

    public AiSyncCleanupResponse cleanupOldWorkiJobs(CleanupTrigger trigger) {
        AiSyncSetting setting = loadSetting();
        int retentionDays = setting.getRetentionDays();

        // 0 = "기한 없음" — 자동 정리를 수행하지 않는다.
        if (retentionDays <= 0) {
            log.info("[AI-SYNC][CLEANUP] 보존 기간 '기한 없음' 설정, 정리 건너뜀: trigger={}", trigger);
            return new AiSyncCleanupResponse(0, 0, 0);
        }

        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        List<AiSyncJob> targets = aiSyncJobRepository.findOldSyncedWorkiLatestJobs(
            cutoff, aiSyncProperties.batchSize());

        int deleted = 0, skipped = 0, failed = 0;
        for (AiSyncJob job : targets) {
            if (aiSyncJobRepository.hasNewerSyncedJob(
                    AiSyncSourceType.WORKI, job.getSourceId(), job.getCreatedAt())) {
                log.info("[AI-SYNC][CLEANUP] 신규 인덱싱 감지, 스킵: sourceId={}", job.getSourceId());
                skipped++;
                continue;
            }
            try {
                textDocumentAiClient.delete(AiSyncSourceType.WORKI, job.getSourceId());
                aiSyncJobRepository.softDeleteOldJobsBySourceId(
                    AiSyncSourceType.WORKI, job.getSourceId(), job.getAiSyncJobId(), LocalDateTime.now());
                deleted++;
            } catch (Exception e) {
                log.warn("[AI-SYNC][CLEANUP] AI 삭제 실패, 스킵: sourceId={}, error={}",
                    job.getSourceId(), e.getMessage());
                failed++;
            }
        }
        log.info("[AI-SYNC][CLEANUP] 완료: trigger={}, deleted={}, skipped={}, failed={}",
            trigger, deleted, skipped, failed);

        aiSyncCleanupLogRepository.save(AiSyncCleanupLog.of(trigger, deleted, skipped, failed));
        return new AiSyncCleanupResponse(deleted, skipped, failed);
    }

    private AiSyncSetting loadSetting() {
        return aiSyncSettingRepository.findFirstByIsDeleted("N")
            .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND));
    }
}
