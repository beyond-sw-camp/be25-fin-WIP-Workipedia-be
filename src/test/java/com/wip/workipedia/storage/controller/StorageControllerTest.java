package com.wip.workipedia.storage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.common.security.InternalApiKeyFilter;
import com.wip.workipedia.common.security.JwtFilter;
import com.wip.workipedia.common.security.JwtProvider;
import com.wip.workipedia.storage.dto.PresignedDownloadResponse;
import com.wip.workipedia.storage.dto.PresignedUploadRequest;
import com.wip.workipedia.storage.dto.PresignedUploadResponse;
import com.wip.workipedia.storage.service.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    value = StorageController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {JwtFilter.class, JwtProvider.class, InternalApiKeyFilter.class}
    )
)
class StorageControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    StorageService storageService;

    @Test
    void presignedUpload_returns_200_with_plain_dto() throws Exception {
        PresignedUploadRequest req = new PresignedUploadRequest("photo.jpg", "image/jpeg");
        PresignedUploadResponse response = new PresignedUploadResponse(
            "https://upload.url", "tickets/replies/uuid/photo.jpg", "https://public.url/photo.jpg");
        given(storageService.createPresignedUploadUrl(any())).willReturn(response);

        mockMvc.perform(post("/api/v1/storage/presigned-upload")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.uploadUrl").value("https://upload.url"))
            .andExpect(jsonPath("$.objectKey").value("tickets/replies/uuid/photo.jpg"))
            .andExpect(jsonPath("$.publicUrl").value("https://public.url/photo.jpg"))
            // ApiResponse 래퍼가 없어야 한다
            .andExpect(jsonPath("$.data").doesNotExist())
            .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void presignedDownload_returns_200_with_plain_dto() throws Exception {
        PresignedDownloadResponse response = new PresignedDownloadResponse("https://download.url");
        given(storageService.createPresignedDownloadUrl(eq("tickets/replies/uuid/photo.jpg")))
            .willReturn(response);

        mockMvc.perform(get("/api/v1/storage/presigned-download")
                .param("objectKey", "tickets/replies/uuid/photo.jpg"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.downloadUrl").value("https://download.url"))
            .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void deleteObject_returns_204() throws Exception {
        mockMvc.perform(delete("/api/v1/storage")
                .param("objectKey", "tickets/replies/uuid/photo.jpg"))
            .andExpect(status().isNoContent());

        verify(storageService).deleteObject("tickets/replies/uuid/photo.jpg");
    }
}
