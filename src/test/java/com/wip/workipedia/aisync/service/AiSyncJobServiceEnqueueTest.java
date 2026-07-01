package com.wip.workipedia.aisync.service;

import com.wip.workipedia.aisync.domain.AiSyncOperation;
import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.aisync.repository.AiSyncJobRepository;
import com.wip.workipedia.config.AiSyncProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiSyncJobServiceEnqueueTest {

    @Mock AiSyncJobRepository aiSyncJobRepository;
    @Mock AiSyncProperties aiSyncProperties;

    @InjectMocks AiSyncJobService aiSyncJobService;

    @Test
    @DisplayName("미완료 잡이 있으면 enqueueIfAbsent는 저장하지 않고 false를 반환한다")
    void enqueueIfAbsent_skipsWhenActiveExists() {
        when(aiSyncJobRepository.existsActiveJob(AiSyncSourceType.KNOWLEDGE_DATA, 1L)).thenReturn(true);

        boolean created = aiSyncJobService.enqueueIfAbsent(
            AiSyncSourceType.KNOWLEDGE_DATA, 1L, AiSyncOperation.UPSERT);

        assertThat(created).isFalse();
        verify(aiSyncJobRepository, never()).save(any());
    }

    @Test
    @DisplayName("미완료 잡이 없으면 enqueueIfAbsent는 저장하고 true를 반환한다")
    void enqueueIfAbsent_savesWhenAbsent() {
        when(aiSyncJobRepository.existsActiveJob(AiSyncSourceType.KNOWLEDGE_DATA, 1L)).thenReturn(false);

        boolean created = aiSyncJobService.enqueueIfAbsent(
            AiSyncSourceType.KNOWLEDGE_DATA, 1L, AiSyncOperation.UPSERT);

        assertThat(created).isTrue();
        verify(aiSyncJobRepository).save(any());
    }
}
