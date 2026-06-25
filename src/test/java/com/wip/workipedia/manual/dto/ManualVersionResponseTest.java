package com.wip.workipedia.manual.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.wip.workipedia.aisync.domain.AiSyncJob;
import com.wip.workipedia.aisync.domain.AiSyncOperation;
import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.manual.domain.Manual;
import com.wip.workipedia.manual.domain.ManualStatus;
import com.wip.workipedia.manual.domain.ManualVersion;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ManualVersionResponseTest {

    private ManualVersion version() {
        Manual manual = Manual.create(null, "소개서", "content", ManualStatus.PUBLISHED, null, "1.0", 1L);
        ReflectionTestUtils.setField(manual, "manualId", 9L);
        ManualVersion version = ManualVersion.create(manual, 1L, "1.0", "FILE_ADDED", null);
        version.applyChangeSummary("요약문");
        return version;
    }

    @Test
    void from_withJob_mapsStatusAndLabel() {
        AiSyncJob job = AiSyncJob.create(AiSyncSourceType.MANUAL_CHANGE_SUMMARY, 1L, AiSyncOperation.UPSERT);
        ManualVersionResponse response = ManualVersionResponse.from(version(), job);

        assertThat(response.changeSummary()).isEqualTo("요약문");
        assertThat(response.updateReasonLabel()).isEqualTo("첨부 파일이 추가되었습니다.");
        assertThat(response.summaryStatus()).isEqualTo("PENDING");
    }

    @Test
    void from_nullJob_statusIsNone() {
        ManualVersionResponse response = ManualVersionResponse.from(version(), null);
        assertThat(response.summaryStatus()).isEqualTo("NONE");
    }

    @Test
    void from_singleArg_delegatesWithNoneStatus() {
        ManualVersionResponse response = ManualVersionResponse.from(version());
        assertThat(response.summaryStatus()).isEqualTo("NONE");
    }
}
