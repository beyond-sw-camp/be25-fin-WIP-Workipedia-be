package com.wip.workipedia.search.dto;

import com.wip.workipedia.common.response.PageResponse;

/**
 * 통합검색 응답. 도메인별로 결과를 분리해 담는다.
 * 각 도메인 {@code pageInfo.totalElements} 로 "워키 N건 / 매뉴얼 N건 · 더보기"를 표시할 수 있다.
 */
public record IntegratedSearchResponse(
        PageResponse<WorkiSearchResponse> worki,
        PageResponse<ManualSearchResponse> manuals
) {
}
