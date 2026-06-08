package com.wip.workipedia.flashchat.controller;

import com.wip.workipedia.flashchat.dto.SendMessageRequest;
import com.wip.workipedia.flashchat.service.FlashChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class FlashChatStompController {

    private final FlashChatService flashChatService;

    @MessageMapping("/flash-chat/send")
    public void send(SendMessageRequest request) {
        flashChatService.sendMessage(request);
    }
}
