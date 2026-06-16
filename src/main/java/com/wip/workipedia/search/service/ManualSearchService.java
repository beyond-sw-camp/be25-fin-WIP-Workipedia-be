package com.wip.workipedia.search.service;

import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.manual.domain.ManualStatus;
import com.wip.workipedia.manual.repository.ManualRepository;
import com.wip.workipedia.search.dto.ManualSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 매뉴얼 키워드 검색 서비스.
 * 워키와 달리 Elasticsearch 색인 없이 정본(MariaDB)을 직접 조회한다(ADR-009 참고).
 * 공개 검색이므로 발행(PUBLISHED) 상태 매뉴얼만 대상으로 한다.
 */
@Service
@RequiredArgsConstructor
public class ManualSearchService {

    private final ManualRepository manualRepository;

    @Transactional(readOnly = true)
    public PageResponse<ManualSearchResponse> searchManuals(String keyword, Pageable pageable) {
        return PageResponse.from(
                manualRepository.searchByKeyword(keyword, ManualStatus.PUBLISHED, pageable)
                        .map(ManualSearchResponse::from)
        );
    }
}
