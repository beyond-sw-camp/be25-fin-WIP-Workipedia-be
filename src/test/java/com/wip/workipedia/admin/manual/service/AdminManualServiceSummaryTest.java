package com.wip.workipedia.admin.manual.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wip.workipedia.aisync.domain.AiSyncOperation;
import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.aisync.service.AiSyncJobService;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.manual.domain.Manual;
import com.wip.workipedia.manual.domain.ManualStatus;
import com.wip.workipedia.manual.domain.ManualVersion;
import com.wip.workipedia.manual.repository.ManualVersionRepository;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.domain.UserRole;
import com.wip.workipedia.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

// resummarize 경로만 단위 검증한다. updateFromPdf의 enqueue 분기는 통합 성격이 강해
// 여기서는 resummarize 검증으로 enqueue 계약(타입/sourceId=versionId)을 고정한다.
@ExtendWith(MockitoExtension.class)
class AdminManualServiceSummaryTest {

    @Mock UserRepository userRepository;
    @Mock ManualVersionRepository manualVersionRepository;
    @Mock AiSyncJobService aiSyncJobService;

    private AdminManualService service() {
        return new AdminManualService(
            null, null, null, manualVersionRepository, null, userRepository,
            null, null, null, null, null, aiSyncJobService, null);
    }

    private User admin() {
        User user = User.signup(mock(Department.class), "employee-1", "admin@example.com", "password", "admin");
        ReflectionTestUtils.setField(user, "role", UserRole.SYSTEM_ADMIN);
        return user;
    }

    private ManualVersion version(Long manualId, String contentDiff) {
        Manual manual = Manual.create(null, "소개서", "content", ManualStatus.PUBLISHED, null, "1.0", 1L);
        ReflectionTestUtils.setField(manual, "manualId", manualId);
        ManualVersion version = ManualVersion.create(manual, 1L, "1.0", "PDF_UPLOAD", contentDiff);
        ReflectionTestUtils.setField(version, "manualVersionId", 50L);
        return version;
    }

    @Test
    void resummarize_validDiff_enqueuesByVersionId() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin()));
        when(manualVersionRepository.findById(50L)).thenReturn(Optional.of(version(9L, "@@ line 1 @@\n+ b")));

        service().resummarize(1L, 9L, 50L);

        verify(aiSyncJobService).enqueue(eq(AiSyncSourceType.MANUAL_CHANGE_SUMMARY), eq(50L), eq(AiSyncOperation.UPSERT));
    }

    @Test
    void resummarize_nullDiff_rejected() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin()));
        when(manualVersionRepository.findById(50L)).thenReturn(Optional.of(version(9L, null)));

        assertThatThrownBy(() -> service().resummarize(1L, 9L, 50L)).isInstanceOf(CustomException.class);
        verify(aiSyncJobService, never()).enqueue(eq(AiSyncSourceType.MANUAL_CHANGE_SUMMARY), eq(50L), eq(AiSyncOperation.UPSERT));
    }

    @Test
    void resummarize_versionOfDifferentManual_rejected() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin()));
        when(manualVersionRepository.findById(50L)).thenReturn(Optional.of(version(9L, "@@ line 1 @@\n+ b")));

        assertThatThrownBy(() -> service().resummarize(1L, 999L, 50L)).isInstanceOf(CustomException.class);
    }
}
