package com.wip.workipedia.search.controller;

import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.search.dto.IntegratedSearchResponse;
import com.wip.workipedia.search.dto.ManualSearchResponse;
import com.wip.workipedia.search.dto.WorkiSearchResponse;
import com.wip.workipedia.search.service.ManualSearchService;
import com.wip.workipedia.search.service.WorkiQuestionIndexer;
import com.wip.workipedia.search.service.WorkiSearchKeywordService;
import com.wip.workipedia.search.service.WorkiSearchService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 통합 검색 창구. 워키 질문(Elasticsearch)과 매뉴얼(DB) 검색을 함께 제공한다.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Validated
public class SearchController {

    private final WorkiSearchService workiSearchService;
    private final ManualSearchService manualSearchService;
    private final WorkiSearchKeywordService workiSearchKeywordService;
    private final WorkiQuestionIndexer workiQuestionIndexer;

    /**
     * 통합 검색. 워키 + 매뉴얼을 도메인별로 분리해 미리보기 크기만큼 반환한다.
     * 예) GET /api/v1/search?keyword=휴가&size=5
     */
    @GetMapping
    public ResponseEntity<IntegratedSearchResponse> searchIntegrated(
            @RequestParam
            @NotBlank
            @Size(min = 2, max = 100)
            String keyword,
            @RequestParam(defaultValue = "5")
            @Positive
            @Max(50)
            int size) {
        Pageable preview = PageRequest.of(0, size);
        PageResponse<WorkiSearchResponse> worki = searchWorkiSafely(keyword, preview);
        PageResponse<ManualSearchResponse> manuals = manualSearchService.searchManuals(keyword, preview);
        workiSearchKeywordService.record(keyword); // 자동완성용 검색어 누적(비동기)
        return ResponseEntity.ok(new IntegratedSearchResponse(worki, manuals));
    }

    /** 매뉴얼 키워드 검색. 예) GET /api/v1/search/manuals?keyword=휴가&page=0&size=10 */
    @GetMapping("/manuals")
    public ResponseEntity<PageResponse<ManualSearchResponse>> searchManuals(
            @RequestParam
            @NotBlank
            @Size(min = 2, max = 100)
            String keyword,
            Pageable pageable) {
        PageResponse<ManualSearchResponse> result = manualSearchService.searchManuals(keyword, pageable);
        workiSearchKeywordService.record(keyword); // 자동완성용 검색어 누적(비동기)
        return ResponseEntity.ok(result);
    }

    // 통합검색에서 워키(ES) 조회가 실패해도 매뉴얼(DB) 결과는 돌려주도록, 워키 쪽 예외는 빈 결과로 격리한다.
    private PageResponse<WorkiSearchResponse> searchWorkiSafely(String keyword, Pageable pageable) {
        try {
            return workiSearchService.searchQuestions(keyword, pageable);
        } catch (Exception e) {
            log.warn("통합검색 워키(ES) 조회 실패. 매뉴얼 결과만 반환한다. keyword={}", keyword, e);
            return PageResponse.from(Page.<WorkiSearchResponse>empty(pageable));
        }
    }

    /** 워키 질문 키워드 검색. 예) GET /api/v1/search/worki?keyword=휴가&page=0&size=10 */
    @GetMapping("/worki")
    public ResponseEntity<PageResponse<WorkiSearchResponse>> searchWorki(
            @RequestParam
            @NotBlank
            @Size(min = 2, max = 100)
            String keyword,
            Pageable pageable) {
        PageResponse<WorkiSearchResponse> result = workiSearchService.searchQuestions(keyword, pageable);
        workiSearchKeywordService.record(keyword); // 자동완성용 검색어 누적(비동기)
        return ResponseEntity.ok(result);
    }

    /** 검색어 자동완성(DB 기반). 검색 많은 순으로 추천. 예) GET /api/v1/search/worki/autocomplete?keyword=휴 */
    @GetMapping("/worki/autocomplete")
    public ResponseEntity<List<String>> autocompleteWorki(
            @RequestParam
            @NotBlank
            @Size(max = 100)
            String keyword) {
        return ResponseEntity.ok(workiSearchKeywordService.autocomplete(keyword));
    }

    // 정기 재색인은 WorkiReindexScheduler가 매일 자정에 수행한다. 이 엔드포인트는 긴급 수동 복구용.
    // TODO: 관리자 전용으로 제한 필요(이슬이 시큐리티 통합 후). 초기 적재/색인 복구용 임시 엔드포인트.
    // DB의 전체내용을 ES에 적재하는 역할.
    @PostMapping("/worki/reindex")
    public ResponseEntity<Long> reindexWorki() {
        return ResponseEntity.ok(workiQuestionIndexer.reindexAll());
    }
}
