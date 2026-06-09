package com.wip.workipedia.worki.controller;

import com.wip.workipedia.worki.dto.QuestionCreateRequest;
import com.wip.workipedia.worki.dto.QuestionDetailResponse;
import com.wip.workipedia.worki.dto.QuestionResponse;
import com.wip.workipedia.worki.dto.QuestionSummaryResponse;
import com.wip.workipedia.worki.dto.QuestionUpdateRequest;
import com.wip.workipedia.worki.service.WorkiQuestionLikeService;
import com.wip.workipedia.worki.service.WorkiQuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/worki/questions")
@RequiredArgsConstructor
public class WorkiQuestionController {

    private final WorkiQuestionService questionService;
    private final WorkiQuestionLikeService likeService;

    @PostMapping
    public ResponseEntity<QuestionResponse> create(
            @AuthenticationPrincipal Long actorUserId,
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

    // 수정
    @PatchMapping("/{questionId}")
    public ResponseEntity<QuestionResponse> update(
            @AuthenticationPrincipal Long actorUserId,
            @PathVariable Long questionId,
            @Valid @RequestBody QuestionUpdateRequest request) {
        return ResponseEntity.ok(questionService.update(actorUserId, questionId, request));
    }

    // 좋아요 등록
    @PostMapping("/{questionId}/like")
    public ResponseEntity<Void> like(
            @AuthenticationPrincipal Long actorUserId,
            @PathVariable Long questionId) {
        likeService.like(actorUserId, questionId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 좋아요 취소
    // 근데 이게 맞나? 논의 필요.
    @DeleteMapping("/{questionId}/like")
    public ResponseEntity<Void> unlike(
            @AuthenticationPrincipal Long actorUserId,
            @PathVariable Long questionId) {
        likeService.unlike(actorUserId, questionId);
        return ResponseEntity.noContent().build();
    }

    // 테스트용 일괄 등록 API. 검색 색인 테스트처럼 여러 건을 한 번에 넣을 때 사용.
    @PostMapping("/bulk")
    public ResponseEntity<List<QuestionResponse>> createBulk(
            @AuthenticationPrincipal Long actorUserId,
            @Valid @RequestBody List<QuestionCreateRequest> requests
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionService.createBulk(actorUserId, requests));
    }
}
