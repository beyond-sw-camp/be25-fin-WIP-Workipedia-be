package com.wip.workipedia.chatbot.controller;

import com.wip.workipedia.chatbot.dto.ChatbotMessageRequest;
import com.wip.workipedia.chatbot.dto.ChatbotMessageResponse;
import com.wip.workipedia.chatbot.dto.ChatbotSessionResponse;
import com.wip.workipedia.chatbot.service.ChatbotService;
import com.wip.workipedia.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chatbot/sessions")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    // POST /api/v1/chatbot/sessions
    // 새 챗봇 세션 생성. title은 선택값(null 허용)
    @PostMapping
    public ResponseEntity<ChatbotSessionResponse> createSession(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String title) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatbotService.createSession(userId, title));
    }

    // GET /api/v1/chatbot/sessions
    // 본인의 세션 목록 최신순 페이징 조회
    @GetMapping
    public ResponseEntity<PageResponse<ChatbotSessionResponse>> getMySessions(
            @AuthenticationPrincipal Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(chatbotService.getMySessions(userId, pageable));
    }

    // GET /api/v1/chatbot/sessions/{sessionId}/messages
    // 세션의 메시지 목록 오래된 순 페이징 조회 (대화 히스토리 표시용)
    @GetMapping("/{sessionId}/messages")
    public ResponseEntity<PageResponse<ChatbotMessageResponse>> getMessages(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long sessionId,
            Pageable pageable) {
        return ResponseEntity.ok(chatbotService.getMessages(userId, sessionId, pageable));
    }

    // POST /api/v1/chatbot/sessions/{sessionId}/messages
    // 질문을 AI에 전송하고 ASSISTANT 응답을 저장 후 반환
    @PostMapping("/{sessionId}/messages")
    public ResponseEntity<ChatbotMessageResponse> sendMessage(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long sessionId,
            @Valid @RequestBody ChatbotMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatbotService.sendMessage(userId, sessionId, request));
    }
}
