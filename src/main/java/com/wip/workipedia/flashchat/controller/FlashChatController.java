package com.wip.workipedia.flashchat.controller;

import com.wip.workipedia.flashchat.dto.FlashChatMessageResponse;
import com.wip.workipedia.flashchat.dto.FlashChatPolicyRequest;
import com.wip.workipedia.flashchat.dto.FlashChatPolicyResponse;
import com.wip.workipedia.flashchat.service.FlashChatService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FlashChatController {

    private final FlashChatService flashChatService;

    @GetMapping("/flash-chat/messages")
    public ResponseEntity<Map<String, List<FlashChatMessageResponse>>> getActiveMessages() {
        return ResponseEntity.ok(Map.of("messages", flashChatService.getActiveMessages()));
    }

    @GetMapping("/admin/flash-chat/policy")
    public ResponseEntity<FlashChatPolicyResponse> getPolicy() {
        return ResponseEntity.ok(flashChatService.getPolicyResponse());
    }

    @PatchMapping("/admin/flash-chat/policy")
    public ResponseEntity<FlashChatPolicyResponse> updatePolicy(
            @AuthenticationPrincipal Long adminUserId,
            @Valid @RequestBody FlashChatPolicyRequest request) {
        return ResponseEntity.ok(flashChatService.updatePolicy(adminUserId, request));
    }

    @DeleteMapping("/admin/flash-chat/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @AuthenticationPrincipal Long adminUserId,
            @PathVariable String messageId) {
        flashChatService.deleteMessage(adminUserId, messageId);
        return ResponseEntity.noContent().build();
    }
}
