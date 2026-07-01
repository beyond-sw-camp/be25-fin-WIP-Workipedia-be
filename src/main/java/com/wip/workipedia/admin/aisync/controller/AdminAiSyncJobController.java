package com.wip.workipedia.admin.aisync.controller;

import com.wip.workipedia.admin.aisync.dto.AiSyncCleanupLogResponse;
import com.wip.workipedia.admin.aisync.dto.AiSyncCleanupResponse;
import com.wip.workipedia.admin.aisync.dto.AiSyncJobListRequest;
import com.wip.workipedia.admin.aisync.dto.AiSyncJobResponse;
import com.wip.workipedia.admin.aisync.dto.AiSyncJobStatsResponse;
import com.wip.workipedia.admin.aisync.dto.AiSyncSettingResponse;
import com.wip.workipedia.admin.aisync.dto.AiSyncSettingUpdateRequest;
import com.wip.workipedia.admin.aisync.dto.KnowledgeSyncRequest;
import com.wip.workipedia.admin.aisync.service.AdminAiSyncJobService;
import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.aisync.domain.CleanupTrigger;
import com.wip.workipedia.aisync.service.AiSyncCleanupService;
import com.wip.workipedia.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/ai-sync-jobs")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
@RequiredArgsConstructor
public class AdminAiSyncJobController {

    private final AdminAiSyncJobService adminAiSyncJobService;
    private final AiSyncCleanupService aiSyncCleanupService;

    @GetMapping
    public ResponseEntity<PageResponse<AiSyncJobResponse>> getJobs(@Valid AiSyncJobListRequest req) {
        return ResponseEntity.ok(adminAiSyncJobService.getJobs(req));
    }

    @GetMapping("/stats")
    public ResponseEntity<AiSyncJobStatsResponse> getStats(
            @RequestParam(required = false) List<AiSyncSourceType> sourceTypes) {
        return ResponseEntity.ok(adminAiSyncJobService.getStats(sourceTypes));
    }

    // 지식 데이터 대기 작업 즉시 실행 (비동기 드레인) — 응답 { "queued": n }
    @PostMapping("/run-now")
    public ResponseEntity<Map<String, Long>> runNow(@RequestBody(required = false) KnowledgeSyncRequest req) {
        return ResponseEntity.ok(adminAiSyncJobService.runNowKnowledge(
            req != null ? req : new KnowledgeSyncRequest(null)));
    }

    // 지식 데이터 전체 재동기화 — 응답 { "enqueued": e, "skipped": s }
    @PostMapping("/resync-knowledge")
    public ResponseEntity<Map<String, Integer>> resyncKnowledge(@RequestBody(required = false) KnowledgeSyncRequest req) {
        return ResponseEntity.ok(adminAiSyncJobService.resyncKnowledge(
            req != null ? req : new KnowledgeSyncRequest(null)));
    }

    @PostMapping("/{jobId}/retry")
    public ResponseEntity<Void> retryJob(@PathVariable Long jobId) {
        adminAiSyncJobService.retryJob(jobId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/retry-all")
    public ResponseEntity<Map<String, Integer>> retryAllFailed() {
        int count = adminAiSyncJobService.retryAllFailed();
        return ResponseEntity.ok(Map.of("retried", count));
    }

    @GetMapping("/settings")
    public ResponseEntity<AiSyncSettingResponse> getSetting() {
        return ResponseEntity.ok(aiSyncCleanupService.getSetting());
    }

    @PutMapping("/settings")
    public ResponseEntity<AiSyncSettingResponse> updateSetting(@Valid @RequestBody AiSyncSettingUpdateRequest req) {
        return ResponseEntity.ok(aiSyncCleanupService.updateSetting(req));
    }

    @PostMapping("/cleanup-worki")
    public ResponseEntity<AiSyncCleanupResponse> cleanupOldWorki() {
        return ResponseEntity.ok(aiSyncCleanupService.cleanupOldWorkiJobs(CleanupTrigger.MANUAL));
    }

    @GetMapping("/cleanup-worki/logs")
    public ResponseEntity<List<AiSyncCleanupLogResponse>> getCleanupLogs(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(aiSyncCleanupService.getRecentLogs(limit));
    }
}
