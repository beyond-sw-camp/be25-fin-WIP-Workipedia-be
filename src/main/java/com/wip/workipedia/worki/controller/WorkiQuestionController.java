package com.wip.workipedia.worki.controller;

import com.wip.workipedia.worki.dto.AnswerCreateRequest;
import com.wip.workipedia.worki.dto.AnswerResponse;
import com.wip.workipedia.worki.dto.QuestionCreateRequest;
import com.wip.workipedia.worki.dto.QuestionDetailResponse;
import com.wip.workipedia.worki.dto.QuestionResponse;
import com.wip.workipedia.worki.dto.QuestionSummaryResponse;
import com.wip.workipedia.worki.dto.QuestionUpdateRequest;
import com.wip.workipedia.worki.service.WorkiAnswerService;
import com.wip.workipedia.worki.service.WorkiQuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/worki/questions")
@RequiredArgsConstructor
public class WorkiQuestionController {

    private final WorkiQuestionService questionService;
    private final WorkiAnswerService answerService;

    // TODO: 이슬이 시큐리티 통합 후 @AuthenticationPrincipal로 교체. 통합 전까지 X-User-Id 헤더로 대체.
    @PostMapping
    public ResponseEntity<QuestionResponse> create(
            @RequestHeader("X-User-Id") Long actorUserId,
            @Valid @RequestBody QuestionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionService.create(actorUserId, request));
    }

    @GetMapping
    public ResponseEntity<Page<QuestionSummaryResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(questionService.list(pageable));
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionDetailResponse> detail(@PathVariable Long questionId) {
        return ResponseEntity.ok(questionService.getDetail(questionId));
    }

    @PatchMapping("/{questionId}")
    public ResponseEntity<QuestionResponse> update(
            @RequestHeader("X-User-Id") Long actorUserId,
            @PathVariable Long questionId,
            @Valid @RequestBody QuestionUpdateRequest request) {
        return ResponseEntity.ok(questionService.update(actorUserId, questionId, request));
    }

    @PostMapping("/{questionId}/answers")
    public ResponseEntity<AnswerResponse> createAnswer(
            @RequestHeader("X-User-Id") Long actorUserId,
            @PathVariable Long questionId,
            @Valid @RequestBody AnswerCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(answerService.createAnswer(actorUserId, questionId, request));
    }
}
