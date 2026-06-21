package com.wip.workipedia.admin.aisync.service;

import com.wip.workipedia.aisync.domain.AiSyncJob;
import com.wip.workipedia.aisync.domain.AiSyncOperation;
import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.aisync.repository.AiSyncJobRepository;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAiSyncJobServiceTest {

    @Mock
    private AiSyncJobRepository aiSyncJobRepository;

    @InjectMocks
    private AdminAiSyncJobService adminAiSyncJobService;

    @Test
    @DisplayName("존재하지 않는 jobId로 재시도 요청 시 NOT_FOUND 예외")
    void retryJob_notFound() {
        when(aiSyncJobRepository.findByAiSyncJobIdAndDeletedAtIsNull(99L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminAiSyncJobService.retryJob(99L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.NOT_FOUND);
    }

    @Test
    @DisplayName("FAILED 아닌 상태 잡에 재시도 요청 시 CONFLICT 예외")
    void retryJob_conflict_notFailed() {
        AiSyncJob job = AiSyncJob.create(AiSyncSourceType.MANUAL, 1L, AiSyncOperation.UPSERT);
        // 기본 상태가 PENDING
        when(aiSyncJobRepository.findByAiSyncJobIdAndDeletedAtIsNull(1L))
            .thenReturn(Optional.of(job));

        assertThatThrownBy(() -> adminAiSyncJobService.retryJob(1L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.CONFLICT);
    }

    @Test
    @DisplayName("전체 FAILED 재시도 시 resetAllFailed 호출 및 건수 반환")
    void retryAllFailed_callsReset() {
        when(aiSyncJobRepository.resetAllFailed()).thenReturn(3);

        int count = adminAiSyncJobService.retryAllFailed();

        verify(aiSyncJobRepository).resetAllFailed();
        assertThat(count).isEqualTo(3);
    }
}
