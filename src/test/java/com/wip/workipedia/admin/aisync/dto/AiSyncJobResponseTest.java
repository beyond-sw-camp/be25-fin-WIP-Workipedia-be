package com.wip.workipedia.admin.aisync.dto;

import com.wip.workipedia.aisync.domain.AiSyncJob;
import com.wip.workipedia.aisync.domain.AiSyncOperation;
import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiSyncJobResponseTest {

    @Test
    @DisplayName("from()은 updatedAt 필드를 매핑한다")
    void from_mapsUpdatedAt() {
        AiSyncJob job = AiSyncJob.create(AiSyncSourceType.KNOWLEDGE_DATA, 1L, AiSyncOperation.UPSERT);

        AiSyncJobResponse res = AiSyncJobResponse.from(job);

        assertThat(res.updatedAt()).isEqualTo(job.getUpdatedAt());
    }
}
