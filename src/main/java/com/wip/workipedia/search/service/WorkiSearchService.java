package com.wip.workipedia.search.service;

import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.search.dto.WorkiSearchResponse;
import com.wip.workipedia.search.repository.WorkiQuestionSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * 워키 질문 키워드 검색 서비스.
 * Elasticsearch에서 title/content를 검색한 뒤, 응답 DTO로 변환해 돌려준다.
 */
@Service
@RequiredArgsConstructor
public class WorkiSearchService {

    private final WorkiQuestionSearchRepository searchRepository;

    public PageResponse<WorkiSearchResponse> searchQuestions(String keyword, Pageable pageable) {
        return PageResponse.from(
                searchRepository.searchByKeyword(keyword, pageable)
                        .map(WorkiSearchResponse::from)
        );
    }
}
