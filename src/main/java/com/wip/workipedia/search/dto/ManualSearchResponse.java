package com.wip.workipedia.search.dto;

import com.wip.workipedia.manual.domain.Manual;
import java.time.LocalDateTime;

/** 매뉴얼 검색 결과 1건. 본문(content)은 응답에 싣지 않고 제목·메타데이터만 내려준다. */
public record ManualSearchResponse(
        Long manualId,
        String title,
        String status,
        Long departmentId,
        String version,
        LocalDateTime createdAt
) {
    public static ManualSearchResponse from(Manual manual) {
        return new ManualSearchResponse(
                manual.getManualId(),
                manual.getTitle(),
                manual.getStatus().name(),
                manual.getDepartmentId(),
                manual.getVersion(),
                manual.getCreatedAt()
        );
    }
}
