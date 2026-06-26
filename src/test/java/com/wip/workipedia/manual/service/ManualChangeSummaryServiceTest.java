package com.wip.workipedia.manual.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class ManualChangeSummaryServiceTest {

    @Mock ManualVersionRepository manualVersionRepository;
    @Mock ManualChangeSummaryAiClient aiClient;

    private ManualChangeSummaryService service;

    @BeforeEach
    void setUp() {
        // 가짜 트랜잭션 매니저로 만든 실제 TransactionTemplate. execute/executeWithoutResult가
        // 콜백을 동기 실행하므로 서비스의 2-트랜잭션 흐름이 실제대로 동작한다.
        TransactionTemplate transactionTemplate =
            new TransactionTemplate(mock(PlatformTransactionManager.class));
        service = new ManualChangeSummaryService(manualVersionRepository, aiClient, transactionTemplate);
    }

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
