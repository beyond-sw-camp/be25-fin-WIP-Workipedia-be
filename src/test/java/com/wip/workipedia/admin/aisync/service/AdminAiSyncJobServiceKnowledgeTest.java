package com.wip.workipedia.admin.aisync.service;

import com.wip.workipedia.admin.aisync.dto.KnowledgeSyncRequest;
import com.wip.workipedia.aisync.domain.AiSyncOperation;
import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.aisync.repository.AiSyncJobRepository;
import com.wip.workipedia.aisync.service.AiSyncJobService;
import com.wip.workipedia.aisync.worker.AiSyncKnowledgeRunner;
import com.wip.workipedia.directdata.repository.DirectDataRepository;
import com.wip.workipedia.knowledge.repository.KnowledgeDataRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAiSyncJobServiceKnowledgeTest {

    @Mock AiSyncJobRepository aiSyncJobRepository;
    @Mock AiSyncJobService aiSyncJobService;
    @Mock AiSyncKnowledgeRunner knowledgeRunner;
    @Mock KnowledgeDataRepository knowledgeDataRepository;
    @Mock DirectDataRepository directDataRepository;

    @InjectMocks AdminAiSyncJobService service;

    @Test
    @DisplayName("runNowKnowledge는 PENDING 건수를 queued로 반환하고 드레인을 트리거한다")
    void runNow_returnsQueuedAndTriggers() {
        when(aiSyncJobRepository.countPendingBySourceTypes(anyList())).thenReturn(7L);

        Map<String, Long> res = service.runNowKnowledge(new KnowledgeSyncRequest(null));

        assertThat(res).containsEntry("queued", 7L);
        verify(knowledgeRunner).drain(anyList());
    }

    @Test
    @DisplayName("resyncKnowledge는 활성 원본 전체를 enqueueIfAbsent 하고 enqueued/skipped 반환")
    void resync_enqueuesActive() {
        when(knowledgeDataRepository.findActiveIds()).thenReturn(List.of(1L, 2L));
        when(directDataRepository.findActiveIds()).thenReturn(List.of(9L));
        when(aiSyncJobService.enqueueIfAbsent(eq(AiSyncSourceType.KNOWLEDGE_DATA), any(), eq(AiSyncOperation.UPSERT)))
            .thenReturn(true, false);
        when(aiSyncJobService.enqueueIfAbsent(eq(AiSyncSourceType.MANUAL_KNOWLEDGE), eq(9L), eq(AiSyncOperation.UPSERT)))
            .thenReturn(true);

        Map<String, Integer> res = service.resyncKnowledge(new KnowledgeSyncRequest(null));

        assertThat(res).containsEntry("enqueued", 2).containsEntry("skipped", 1);
    }
}
