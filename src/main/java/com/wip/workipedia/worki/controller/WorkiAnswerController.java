package com.wip.workipedia.worki.controller;

import com.wip.workipedia.worki.dto.AnswerResponse;
import com.wip.workipedia.worki.service.WorkiAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/worki/answers")
@RequiredArgsConstructor
public class WorkiAnswerController {

    private final WorkiAnswerService answerService;

    // TODO: 이슬이 시큐리티 통합 후 @AuthenticationPrincipal로 교체. 통합 전까지 X-User-Id 헤더로 대체.
    @PostMapping("/{answerId}/accept")
    public ResponseEntity<AnswerResponse> accept(
            @RequestHeader("X-User-Id") Long actorUserId,
            @PathVariable Long answerId) {
        return ResponseEntity.ok(answerService.acceptAnswer(actorUserId, answerId));
    }
}
