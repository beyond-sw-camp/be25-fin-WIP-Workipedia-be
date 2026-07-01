package com.wip.workipedia.aisync.worker;

import com.wip.workipedia.aisync.domain.AiSyncJob;
import com.wip.workipedia.aisync.domain.AiSyncOperation;
import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.aisync.service.AiSyncJobService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiSyncKnowledgeRunnerTest {

    @Mock AiSyncJobService aiSyncJobService;
    @Mock AiSyncJobProcessor processor;

    @InjectMocks AiSyncKnowledgeRunner runner;

    @Test
    @DisplayName("drain은 claim 결과가 빌 때까지 반복 후 종료한다")
    void drain_loopsUntilEmpty() {
        List<AiSyncSourceType> scope = List.of(AiSyncSourceType.KNOWLEDGE_DATA);
        AiSyncJob job = AiSyncJob.create(AiSyncSourceType.KNOWLEDGE_DATA, 1L, AiSyncOperation.UPSERT);

        when(aiSyncJobService.claimPendingKnowledgeJobs(scope))
            .thenReturn(List.of(job))
            .thenReturn(List.of());

        runner.drain(scope);

        verify(processor, times(1)).processJobs(anyList());
        verify(aiSyncJobService, times(2)).claimPendingKnowledgeJobs(scope);
    }
}
