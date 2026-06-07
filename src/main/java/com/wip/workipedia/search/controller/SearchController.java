package com.wip.workipedia.search.controller;

import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.search.dto.WorkiSearchResponse;
import com.wip.workipedia.search.service.WorkiQuestionIndexer;
import com.wip.workipedia.search.service.WorkiSearchService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 통합 검색 창구. 현재는 워키 질문 검색만 제공하며, 추후 매뉴얼 검색을 같은 패키지에 추가한다.
 */
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final WorkiSearchService workiSearchService;
    private final WorkiQuestionIndexer workiQuestionIndexer;

    /** 워키 질문 키워드 검색. 예) GET /api/v1/search/worki?keyword=휴가&page=0&size=10 */
    @GetMapping("/worki")
    public ResponseEntity<PageResponse<WorkiSearchResponse>> searchWorki(
            @RequestParam 
            @NotBlank
            @Size(min = 2, max = 100) 
            String keyword,
            Pageable pageable) {
        return ResponseEntity.ok(workiSearchService.searchQuestions(keyword, pageable));
    }

    // TODO: 관리자 전용으로 제한 필요(이슬이 시큐리티 통합 후). 초기 적재/색인 복구용 임시 엔드포인트.
    // DB의 전체내용을 ES에 적재하는 역할.
    @PostMapping("/worki/reindex")
    public ResponseEntity<Long> reindexWorki() {
        return ResponseEntity.ok(workiQuestionIndexer.reindexAll());
    }
}
