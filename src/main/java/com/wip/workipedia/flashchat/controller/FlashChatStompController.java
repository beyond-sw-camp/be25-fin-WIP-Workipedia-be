package com.wip.workipedia.flashchat.controller;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.flashchat.dto.SendMessageRequest;
import com.wip.workipedia.flashchat.service.FlashChatService;
import java.security.Principal;
import com.wip.workipedia.flashchat.dto.SendMessageRequest;
import com.wip.workipedia.flashchat.service.FlashChatService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class FlashChatStompController {

    private final FlashChatService flashChatService;

    @MessageMapping("/flash-chat/send")
    public void send(SendMessageRequest request, Principal principal) {
        if (principal == null) {
            throw new CustomException(ErrorType.UNAUTHORIZED);
        }
        flashChatService.sendMessage(Long.parseLong(principal.getName()), request);
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public Map<String, String> handleException(CustomException e) {
        return Map.of(
            "status", e.getErrorType().getStatus(),
            "message", e.getErrorType().getMessage()
        );
    }
}
