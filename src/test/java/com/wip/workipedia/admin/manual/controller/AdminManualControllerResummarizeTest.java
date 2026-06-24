package com.wip.workipedia.admin.manual.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.wip.workipedia.admin.manual.service.AdminManualService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AdminManualControllerResummarizeTest {

    @Mock AdminManualService adminManualService;
    @InjectMocks AdminManualController controller;

    @Test
    void resummarize_delegatesToServiceAndReturnsOk() {
        ResponseEntity<Void> response = controller.resummarize(1L, 9L, 50L);

        verify(adminManualService).resummarize(1L, 9L, 50L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
