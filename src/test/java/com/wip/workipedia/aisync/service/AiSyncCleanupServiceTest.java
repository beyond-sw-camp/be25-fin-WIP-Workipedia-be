package com.wip.workipedia.aisync.service;

import com.wip.workipedia.admin.aisync.dto.AiSyncCleanupResponse;
import com.wip.workipedia.aisync.client.TextDocumentAiClient;
import com.wip.workipedia.aisync.domain.AiSyncJob;
import com.wip.workipedia.aisync.domain.AiSyncOperation;
import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.aisync.repository.AiSyncJobRepository;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.config.AiSyncProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiSyncCleanupServiceTest {

    @Mock
    private AiSyncJobRepository aiSyncJobRepository;

    @Mock
    private TextDocumentAiClient textDocumentAiClient;

    @Mock
    private AiSyncProperties aiSyncProperties;

    @InjectMocks
    private AiSyncCleanupService aiSyncCleanupService;

    @Test
    @DisplayName("오래된 SYNCED WORKI 잡이 없으면 0건 반환")
    void cleanupOldWorkiJobs_empty() {
        when(aiSyncProperties.retentionDays()).thenReturn(365);
        when(aiSyncProperties.batchSize()).thenReturn(5);
        when(aiSyncJobRepository.findOldSyncedWorkiLatestJobs(any(), eq(5)))
            .thenReturn(List.of());

        AiSyncCleanupResponse result = aiSyncCleanupService.cleanupOldWorkiJobs();

        assertThat(result.deleted()).isEqualTo(0);
        assertThat(result.skipped()).isEqualTo(0);
        assertThat(result.failed()).isEqualTo(0);
    }

    @Test
    @DisplayName("AI 서버 삭제 성공 시 soft delete 호출 및 deleted 카운트 증가")
    void cleanupOldWorkiJobs_success() {
        when(aiSyncProperties.retentionDays()).thenReturn(365);
        when(aiSyncProperties.batchSize()).thenReturn(5);

        AiSyncJob job = AiSyncJob.create(AiSyncSourceType.WORKI, 42L, AiSyncOperation.UPSERT);
        when(aiSyncJobRepository.findOldSyncedWorkiLatestJobs(any(), eq(5)))
            .thenReturn(List.of(job));
        when(aiSyncJobRepository.hasNewerSyncedJob(
            eq(AiSyncSourceType.WORKI), eq(42L), any()))
            .thenReturn(false);
        when(aiSyncJobRepository.softDeleteOldJobsBySourceId(
            eq(AiSyncSourceType.WORKI), eq(42L), any(), any()))
            .thenReturn(1);

        AiSyncCleanupResponse result = aiSyncCleanupService.cleanupOldWorkiJobs();

        verify(textDocumentAiClient).delete(AiSyncSourceType.WORKI, 42L);
        verify(aiSyncJobRepository).softDeleteOldJobsBySourceId(
            eq(AiSyncSourceType.WORKI), eq(42L), any(), any());
        assertThat(result.deleted()).isEqualTo(1);
        assertThat(result.skipped()).isEqualTo(0);
        assertThat(result.failed()).isEqualTo(0);
    }

    @Test
    @DisplayName("조회 후 새 SYNCED 잡이 생기면 AI DELETE 스킵하고 skipped 카운트 증가")
    void cleanupOldWorkiJobs_skipWhenNewerJobExists() {
        when(aiSyncProperties.retentionDays()).thenReturn(365);
        when(aiSyncProperties.batchSize()).thenReturn(5);

        AiSyncJob job = AiSyncJob.create(AiSyncSourceType.WORKI, 77L, AiSyncOperation.UPSERT);
        when(aiSyncJobRepository.findOldSyncedWorkiLatestJobs(any(), eq(5)))
            .thenReturn(List.of(job));
        when(aiSyncJobRepository.hasNewerSyncedJob(
            eq(AiSyncSourceType.WORKI), eq(77L), any()))
            .thenReturn(true);

        AiSyncCleanupResponse result = aiSyncCleanupService.cleanupOldWorkiJobs();

        verify(textDocumentAiClient, never()).delete(any(), any());
        verify(aiSyncJobRepository, never()).softDeleteOldJobsBySourceId(any(), any(), any(), any());
        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.deleted()).isEqualTo(0);
    }

    @Test
    @DisplayName("AI 서버 삭제 실패 시 soft delete 스킵하고 failed 카운트 증가")
    void cleanupOldWorkiJobs_aiServerFails() {
        when(aiSyncProperties.retentionDays()).thenReturn(365);
        when(aiSyncProperties.batchSize()).thenReturn(5);

        AiSyncJob job = AiSyncJob.create(AiSyncSourceType.WORKI, 99L, AiSyncOperation.UPSERT);
        when(aiSyncJobRepository.findOldSyncedWorkiLatestJobs(any(), eq(5)))
            .thenReturn(List.of(job));
        when(aiSyncJobRepository.hasNewerSyncedJob(
            eq(AiSyncSourceType.WORKI), eq(99L), any()))
            .thenReturn(false);
        doThrow(new CustomException(ErrorType.AI_SYNC_FAILED))
            .when(textDocumentAiClient).delete(AiSyncSourceType.WORKI, 99L);

        AiSyncCleanupResponse result = aiSyncCleanupService.cleanupOldWorkiJobs();

        verify(aiSyncJobRepository, never()).softDeleteOldJobsBySourceId(any(), any(), any(), any());
        assertThat(result.failed()).isEqualTo(1);
        assertThat(result.deleted()).isEqualTo(0);
    }
}
