package com.wip.workipedia.manual.controller;

import com.wip.workipedia.common.request.BasePageRequest;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.manual.dto.ManualDetailResponse;
import com.wip.workipedia.manual.dto.ManualSummaryResponse;
import com.wip.workipedia.manual.service.ManualService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 여기의 경우는 일반 사용자가 사용하는 하나의 컨트롤러
@RestController
@RequestMapping("/api/v1/manuals")
@RequiredArgsConstructor
public class ManualController {

    private final ManualService manualService;

    @GetMapping
    public ResponseEntity<PageResponse<ManualSummaryResponse>> list(@Valid BasePageRequest pageRequest) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt"); // 최신순 정렬.
        return ResponseEntity.ok(manualService.findPublished(pageRequest.toPageable(sort)));
    }

    // 메뉴얼 상세조회. 사용자는 published만 볼수 있음
    @GetMapping("/{manualId}")
    public ResponseEntity<ManualDetailResponse> detail(@PathVariable Long manualId) {
        return ResponseEntity.ok(manualService.findPublishedById(manualId));
    }
}
