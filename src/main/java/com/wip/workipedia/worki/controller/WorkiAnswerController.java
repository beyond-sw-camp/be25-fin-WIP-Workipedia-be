package com.wip.workipedia.worki.controller;

import com.wip.workipedia.worki.dto.AnswerCreateRequest;
import com.wip.workipedia.worki.dto.AnswerResponse;
import com.wip.workipedia.worki.service.WorkiAnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// URL은 그대로 유지하되 답변 관련 작업을 한 클래스로 모음(생성, 채택, 추후 GET/PATCH 등).
@RestController
@RequestMapping("/api/v1/worki")
@RequiredArgsConstructor
public class WorkiAnswerController {

    private final WorkiAnswerService answerService;

    // TODO: 이슬이 시큐리티 통합 후 @AuthenticationPrincipal로 교체. 통합 전까지 X-User-Id 헤더로 대체.
    
    // 답변 생성
    @PostMapping("/questions/{questionId}/answers")
    public ResponseEntity<AnswerResponse> createAnswer(
            @RequestHeader("X-User-Id") Long actorUserId,
            @PathVariable Long questionId,
            @Valid @RequestBody AnswerCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(answerService.createAnswer(actorUserId, questionId, request));
    }

    // 답변 채택
    @PostMapping("/answers/{answerId}/accept")
    public ResponseEntity<AnswerResponse> accept(
            @RequestHeader("X-User-Id") Long actorUserId,
            @PathVariable Long answerId) {
        return ResponseEntity.ok(answerService.acceptAnswer(actorUserId, answerId));
    }
}
