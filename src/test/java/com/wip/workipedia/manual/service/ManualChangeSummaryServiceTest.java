package com.wip.workipedia.manual.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.manual.ai.ManualChangeSummaryAiClient;
import com.wip.workipedia.manual.ai.dto.ManualChangeSummaryRequest;
import com.wip.workipedia.manual.domain.Manual;
import com.wip.workipedia.manual.domain.ManualStatus;
import com.wip.workipedia.manual.domain.ManualVersion;
import com.wip.workipedia.manual.repository.ManualVersionRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ManualChangeSummaryServiceTest {

    @Mock ManualVersionRepository manualVersionRepository;
    @Mock ManualChangeSummaryAiClient aiClient;
    @InjectMocks ManualChangeSummaryService service;

    private ManualVersion versionWithDiff() {
        Manual manual = Manual.create(null, "소개서", "content", ManualStatus.PUBLISHED, null, "1.0", 1L);
        return ManualVersion.create(manual, 1L, "1.0", "PDF_UPLOAD", "@@ line 1 @@\n- a\n+ b");
    }

    @Test
    void summarize_savesAiResult() {
        ManualVersion version = versionWithDiff();
        when(manualVersionRepository.findById(7L)).thenReturn(Optional.of(version));
        when(aiClient.summarize(any(ManualChangeSummaryRequest.class)))
            .thenReturn("소개서 문구가 수정되었습니다.");

        service.summarize(7L);

        assertThat(version.getChangeSummary()).isEqualTo("소개서 문구가 수정되었습니다.");
    }

    @Test
    void summarize_versionNotFound_throws() {
        when(manualVersionRepository.findById(7L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.summarize(7L))
            .isInstanceOf(CustomException.class);
    }

    @Test
    void summarize_aiFails_propagatesAndDoesNotSave() {
        ManualVersion version = versionWithDiff();
        when(manualVersionRepository.findById(7L)).thenReturn(Optional.of(version));
        when(aiClient.summarize(any(ManualChangeSummaryRequest.class)))
            .thenThrow(new CustomException(ErrorType.AI_SYNC_FAILED));

        assertThatThrownBy(() -> service.summarize(7L)).isInstanceOf(CustomException.class);
        assertThat(version.getChangeSummary()).isNull();
    }
}
