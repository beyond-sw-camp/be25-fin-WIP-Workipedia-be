package com.wip.workipedia.admin.aisync.controller;

import com.wip.workipedia.admin.aisync.dto.AiSyncCleanupResponse;
import com.wip.workipedia.admin.aisync.dto.AiSyncJobListRequest;
import com.wip.workipedia.admin.aisync.dto.AiSyncJobResponse;
import com.wip.workipedia.admin.aisync.dto.AiSyncJobStatsResponse;
import com.wip.workipedia.admin.aisync.service.AdminAiSyncJobService;
import com.wip.workipedia.aisync.service.AiSyncCleanupService;
import com.wip.workipedia.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<AiSyncJobStatsResponse> getStats() {
        return ResponseEntity.ok(adminAiSyncJobService.getStats());
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

    @PostMapping("/cleanup-worki")
    public ResponseEntity<AiSyncCleanupResponse> cleanupOldWorki() {
        return ResponseEntity.ok(aiSyncCleanupService.cleanupOldWorkiJobs());
    }
}
