package com.wip.workipedia.search.service;

import com.wip.workipedia.search.domain.WorkiSearchKeyword;
import com.wip.workipedia.search.repository.WorkiSearchKeywordRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 검색어 자동완성(DB 기반).
 * - record(): 사용자가 검색한 단어를 누적 집계. 검색 응답 속도에 영향을 주지 않도록 비동기로 처리하고,
 *   기록 실패가 검색 자체를 막지 않도록 예외를 삼킨다.
 * - autocomplete(): prefix로 시작하는 검색어를 인기순(검색 많은 순)으로 추천.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkiSearchKeywordService {

    private final WorkiSearchKeywordRepository keywordRepository;

    @Async
    @Transactional
    public void record(String keyword) {
        String trimmed = keyword.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        try {
            keywordRepository.upsertKeyword(trimmed);
        } catch (Exception e) {
            log.warn("검색어 기록 실패 keyword={}", trimmed, e);
        }
    }

    @Transactional(readOnly = true)
    public List<String> autocomplete(String prefix) {
        String trimmed = prefix.trim();
        if (trimmed.isEmpty()) {
            return List.of();
        }
        return keywordRepository.findTop10ByKeywordStartingWithOrderBySearchCountDesc(trimmed)
                .stream()
                .map(WorkiSearchKeyword::getKeyword)
                .toList();
    }
}
