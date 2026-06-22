package com.wip.workipedia.aisync.service;

import com.wip.workipedia.admin.aisync.dto.AiSyncCleanupResponse;
import com.wip.workipedia.aisync.client.TextDocumentAiClient;
import com.wip.workipedia.aisync.domain.AiSyncCleanupLog;
import com.wip.workipedia.aisync.domain.AiSyncJob;
import com.wip.workipedia.aisync.domain.AiSyncOperation;
import com.wip.workipedia.aisync.domain.AiSyncSetting;
import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.aisync.domain.CleanupTrigger;
import com.wip.workipedia.aisync.repository.AiSyncCleanupLogRepository;
import com.wip.workipedia.aisync.repository.AiSyncJobRepository;
import com.wip.workipedia.aisync.repository.AiSyncSettingRepository;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.config.AiSyncProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiSyncCleanupServiceTest {

    @Mock
    private AiSyncJobRepository aiSyncJobRepository;

    @Mock
    private AiSyncSettingRepository aiSyncSettingRepository;

    @Mock
    private AiSyncCleanupLogRepository aiSyncCleanupLogRepository;

    @Mock
    private TextDocumentAiClient textDocumentAiClient;

    @Mock
    private AiSyncProperties aiSyncProperties;

    @InjectMocks
    private AiSyncCleanupService aiSyncCleanupService;

    @Mock
    private AiSyncSetting aiSyncSetting;

    @BeforeEach
    void setUp() {
        // 보존 기간은 DB(ai_sync_settings)에서 조회한다.
        lenient().when(aiSyncSettingRepository.findFirstByIsDeleted("N"))
            .thenReturn(Optional.of(aiSyncSetting));
        lenient().when(aiSyncSetting.getRetentionDays()).thenReturn(365);
        lenient().when(aiSyncProperties.batchSize()).thenReturn(5);
    }

    @Test
    @DisplayName("오래된 SYNCED WORKI 잡이 없으면 0건 반환")
    void cleanupOldWorkiJobs_empty() {
        when(aiSyncJobRepository.findOldSyncedWorkiLatestJobs(any(), eq(5)))
            .thenReturn(List.of());

        AiSyncCleanupResponse result = aiSyncCleanupService.cleanupOldWorkiJobs(CleanupTrigger.MANUAL);

        assertThat(result.deleted()).isEqualTo(0);
        assertThat(result.skipped()).isEqualTo(0);
        assertThat(result.failed()).isEqualTo(0);
        verify(aiSyncCleanupLogRepository).save(any(AiSyncCleanupLog.class));
    }

    @Test
    @DisplayName("AI 서버 삭제 성공 시 soft delete 호출 및 deleted 카운트 증가")
    void cleanupOldWorkiJobs_success() {
        AiSyncJob job = AiSyncJob.create(AiSyncSourceType.WORKI, 42L, AiSyncOperation.UPSERT);
        when(aiSyncJobRepository.findOldSyncedWorkiLatestJobs(any(), eq(5)))
            .thenReturn(List.of(job));
        when(aiSyncJobRepository.hasNewerSyncedJob(
            eq(AiSyncSourceType.WORKI), eq(42L), any()))
            .thenReturn(false);
        when(aiSyncJobRepository.softDeleteOldJobsBySourceId(
            eq(AiSyncSourceType.WORKI), eq(42L), any(), any()))
            .thenReturn(1);

        AiSyncCleanupResponse result = aiSyncCleanupService.cleanupOldWorkiJobs(CleanupTrigger.MANUAL);

        verify(textDocumentAiClient).delete(AiSyncSourceType.WORKI, 42L);
        verify(aiSyncJobRepository).softDeleteOldJobsBySourceId(
            eq(AiSyncSourceType.WORKI), eq(42L), any(), any());
        assertThat(result.deleted()).isEqualTo(1);
        assertThat(result.skipped()).isEqualTo(0);
        assertThat(result.failed()).isEqualTo(0);
        verify(aiSyncCleanupLogRepository).save(any(AiSyncCleanupLog.class));
    }

    @Test
    @DisplayName("조회 후 새 SYNCED 잡이 생기면 AI DELETE 스킵하고 skipped 카운트 증가")
    void cleanupOldWorkiJobs_skipWhenNewerJobExists() {
        AiSyncJob job = AiSyncJob.create(AiSyncSourceType.WORKI, 77L, AiSyncOperation.UPSERT);
        when(aiSyncJobRepository.findOldSyncedWorkiLatestJobs(any(), eq(5)))
            .thenReturn(List.of(job));
        when(aiSyncJobRepository.hasNewerSyncedJob(
            eq(AiSyncSourceType.WORKI), eq(77L), any()))
            .thenReturn(true);

        AiSyncCleanupResponse result = aiSyncCleanupService.cleanupOldWorkiJobs(CleanupTrigger.MANUAL);

        verify(textDocumentAiClient, never()).delete(any(), any());
        verify(aiSyncJobRepository, never()).softDeleteOldJobsBySourceId(any(), any(), any(), any());
        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.deleted()).isEqualTo(0);
    }

    @Test
    @DisplayName("보존 기간 0(기한 없음)이면 정리를 건너뛰고 0건 반환")
    void cleanupOldWorkiJobs_noRetention() {
        when(aiSyncSetting.getRetentionDays()).thenReturn(0);

        AiSyncCleanupResponse result = aiSyncCleanupService.cleanupOldWorkiJobs(CleanupTrigger.SCHEDULE);

        verify(aiSyncJobRepository, never()).findOldSyncedWorkiLatestJobs(any(), org.mockito.ArgumentMatchers.anyInt());
        verify(textDocumentAiClient, never()).delete(any(), any());
        verify(aiSyncCleanupLogRepository, never()).save(any(AiSyncCleanupLog.class));
        assertThat(result.deleted()).isEqualTo(0);
        assertThat(result.skipped()).isEqualTo(0);
        assertThat(result.failed()).isEqualTo(0);
    }

    @Test
    @DisplayName("AI 서버 삭제 실패 시 soft delete 스킵하고 failed 카운트 증가")
    void cleanupOldWorkiJobs_aiServerFails() {
        AiSyncJob job = AiSyncJob.create(AiSyncSourceType.WORKI, 99L, AiSyncOperation.UPSERT);
        when(aiSyncJobRepository.findOldSyncedWorkiLatestJobs(any(), eq(5)))
            .thenReturn(List.of(job));
        when(aiSyncJobRepository.hasNewerSyncedJob(
            eq(AiSyncSourceType.WORKI), eq(99L), any()))
            .thenReturn(false);
        doThrow(new CustomException(ErrorType.AI_SYNC_FAILED))
            .when(textDocumentAiClient).delete(AiSyncSourceType.WORKI, 99L);

        AiSyncCleanupResponse result = aiSyncCleanupService.cleanupOldWorkiJobs(CleanupTrigger.MANUAL);

        verify(aiSyncJobRepository, never()).softDeleteOldJobsBySourceId(any(), any(), any(), any());
        assertThat(result.failed()).isEqualTo(1);
        assertThat(result.deleted()).isEqualTo(0);
    }
}
