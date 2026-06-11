package com.wip.workipedia.flashchat.controller;

import com.wip.workipedia.flashchat.dto.FlashChatMessageResponse;
import com.wip.workipedia.flashchat.service.FlashChatService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 일반 사용자용 FlashChat REST API. 현재 활성 메시지 목록 조회만 제공.
// 메시지 전송은 STOMP(FlashChatStompController)를 통해 처리.
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FlashChatController {

    private final FlashChatService flashChatService;

    @GetMapping("/flash-chat/messages")
    public ResponseEntity<Map<String, List<FlashChatMessageResponse>>> getActiveMessages() {
        return ResponseEntity.ok(Map.of("messages", flashChatService.getActiveMessages()));
    }
}
