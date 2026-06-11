package com.wip.workipedia.flashchat.controller;

import com.wip.workipedia.common.security.JwtFilter;
import com.wip.workipedia.common.security.JwtProvider;
import com.wip.workipedia.flashchat.dto.FlashChatMessageResponse;
import com.wip.workipedia.flashchat.service.FlashChatService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = FlashChatController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtFilter.class, JwtProvider.class}
        )
)
class FlashChatControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean FlashChatService flashChatService;

    @Test
    void getActiveMessages_활성_메시지_목록_반환() throws Exception {
        FlashChatMessageResponse msg = new FlashChatMessageResponse(
                "uuid-1", 1L, "노잇0001", "연차 반차 차이?", null,
                LocalDateTime.of(2026, 6, 8, 10, 0),
                LocalDateTime.of(2026, 6, 8, 10, 10));
        given(flashChatService.getActiveMessages()).willReturn(List.of(msg));

        mockMvc.perform(get("/api/v1/flash-chat/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages[0].id").value("uuid-1"))
                .andExpect(jsonPath("$.messages[0].content").value("연차 반차 차이?"));
    }
}
