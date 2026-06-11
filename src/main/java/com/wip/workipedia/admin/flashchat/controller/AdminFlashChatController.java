package com.wip.workipedia.admin.flashchat.controller;

import com.wip.workipedia.admin.flashchat.dto.FlashChatPolicyRequest;
import com.wip.workipedia.admin.flashchat.dto.FlashChatPolicyResponse;
import com.wip.workipedia.admin.flashchat.service.AdminFlashChatService;
import jakarta.validation.Valid;
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

// 관리자용 FlashChat REST API. 정책 조회·수정과 메시지 강제 삭제를 제공.
// 모든 변경은 AdminLog에 기록되며, 삭제 시 WebSocket으로 클라이언트에 즉시 반영.
@RestController
@RequestMapping("/api/v1/admin/flash-chat")
@RequiredArgsConstructor
public class AdminFlashChatController {

    private final AdminFlashChatService adminFlashChatService;

    @GetMapping("/policy")
    public ResponseEntity<FlashChatPolicyResponse> getPolicy() {
        return ResponseEntity.ok(adminFlashChatService.getPolicyResponse());
    }

    @PatchMapping("/policy")
    public ResponseEntity<FlashChatPolicyResponse> updatePolicy(
            @AuthenticationPrincipal Long adminUserId,
            @Valid @RequestBody FlashChatPolicyRequest request) {
        return ResponseEntity.ok(adminFlashChatService.updatePolicy(adminUserId, request));
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @AuthenticationPrincipal Long adminUserId,
            @PathVariable String messageId) {
        adminFlashChatService.deleteMessage(adminUserId, messageId);
        return ResponseEntity.noContent().build();
    }
}
