package com.wip.workipedia.search.service;

import com.wip.workipedia.search.dto.WorkiAutocompleteResponse;
import com.wip.workipedia.worki.repository.WorkiQuestionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 검색어 자동완성 서비스 (DB 기반, ES/Redis 미사용).
 * 제목 prefix(LIKE 'prefix%') 조회는 인덱스를 탈 수 있어 DB만으로도 충분히 빠르다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkiAutocompleteService {

    private final WorkiQuestionRepository questionRepository;

    public List<WorkiAutocompleteResponse> autocomplete(String prefix) {
        String trimmed = prefix.trim();
        if (trimmed.isEmpty()) {
            return List.of();
        }
        return questionRepository
                .findTop10ByDeletedAtIsNullAndTitleStartingWithOrderByViewCountDesc(trimmed)
                .stream()
                .map(WorkiAutocompleteResponse::from)
                .toList();
    }
}
