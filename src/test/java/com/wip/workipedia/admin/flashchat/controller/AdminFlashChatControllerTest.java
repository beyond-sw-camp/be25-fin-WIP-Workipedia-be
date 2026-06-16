package com.wip.workipedia.admin.flashchat.controller;

import com.wip.workipedia.admin.flashchat.dto.FlashChatPolicyResponse;
import com.wip.workipedia.admin.flashchat.service.AdminFlashChatService;
import com.wip.workipedia.common.security.InternalApiKeyFilter;
import com.wip.workipedia.common.security.JwtFilter;
import com.wip.workipedia.common.security.JwtProvider;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = AdminFlashChatController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtFilter.class, JwtProvider.class, InternalApiKeyFilter.class}
        )
)
class AdminFlashChatControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AdminFlashChatService adminFlashChatService;

    @Test
    void getPolicy_정책_조회() throws Exception {
        given(adminFlashChatService.getPolicyResponse())
                .willReturn(new FlashChatPolicyResponse(600, 0, List.of()));

        mockMvc.perform(get("/api/v1/admin/flash-chat/policy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageTtlSeconds").value(600))
                .andExpect(jsonPath("$.sendCooldownSeconds").value(0));
    }

    @Test
    void deleteMessage_강제삭제_성공() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/flash-chat/messages/uuid-1")
                        .with(authentication(auth(1L))))
                .andExpect(status().isNoContent());

        verify(adminFlashChatService).deleteMessage(nullable(Long.class), eq("uuid-1"));
    }

    @Test
    void updatePolicy_정책_변경() throws Exception {
        given(adminFlashChatService.updatePolicy(nullable(Long.class), any()))
                .willReturn(new FlashChatPolicyResponse(300, 5, List.of("욕설")));

        mockMvc.perform(patch("/api/v1/admin/flash-chat/policy")
                        .with(authentication(auth(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "messageTtlSeconds": 300,
                                  "sendCooldownSeconds": 5,
                                  "bannedWords": ["욕설"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageTtlSeconds").value(300));
    }

    private UsernamePasswordAuthenticationToken auth(Long userId) {
        return new UsernamePasswordAuthenticationToken(userId, null, List.of());
    }
}
