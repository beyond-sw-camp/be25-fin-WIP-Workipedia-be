package com.wip.workipedia.manual.controller;

import com.wip.workipedia.common.request.BasePageRequest;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.manual.dto.ManualDetailResponse;
import com.wip.workipedia.manual.dto.ManualListSortType;
import com.wip.workipedia.manual.dto.ManualSummaryResponse;
import com.wip.workipedia.manual.service.ManualService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/manuals")
@RequiredArgsConstructor
public class ManualController {

	private final ManualService manualService;

	@GetMapping
	public ResponseEntity<PageResponse<ManualSummaryResponse>> list(
		@Valid BasePageRequest pageRequest,
		@RequestParam(defaultValue = "RECENTLY_UPDATED") ManualListSortType sortType
	) {
		Sort sort = toSort(sortType);
		return ResponseEntity.ok(manualService.findPublished(pageRequest.toPageable(sort)));
	}

	@GetMapping("/{manualId}")
	public ResponseEntity<ManualDetailResponse> detail(@PathVariable Long manualId) {
		return ResponseEntity.ok(manualService.findPublishedById(manualId));
	}

	private Sort toSort(ManualListSortType sortType) {
		return switch (sortType) {
			case RECENTLY_UPDATED -> Sort.by(
				Sort.Order.desc("updatedAt"),
				Sort.Order.desc("manualId")
			);
			case RECENTLY_CREATED -> Sort.by(
				Sort.Order.desc("createdAt"),
				Sort.Order.desc("manualId")
			);
		};
	}
}
