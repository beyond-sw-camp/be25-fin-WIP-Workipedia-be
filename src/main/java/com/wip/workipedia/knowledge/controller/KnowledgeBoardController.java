package com.wip.workipedia.knowledge.controller;

import com.wip.workipedia.common.request.BasePageRequest;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.knowledge.dto.KnowledgeBoardResponse;
import com.wip.workipedia.knowledge.service.KnowledgeBoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/knowledge-data")
@RequiredArgsConstructor
public class KnowledgeBoardController {

	private final KnowledgeBoardService knowledgeBoardService;

	@GetMapping
	public ResponseEntity<PageResponse<KnowledgeBoardResponse>> findAll(
		@Valid BasePageRequest pageRequest
	) {
		Sort sort = Sort.by(Sort.Direction.DESC, "approvedAt");
		return ResponseEntity.ok(knowledgeBoardService.findAll(pageRequest.toPageable(sort)));
	}

	@GetMapping("/{knowledgeDataId}")
	public ResponseEntity<KnowledgeBoardResponse> findById(
		@PathVariable Long knowledgeDataId
	) {
		return ResponseEntity.ok(knowledgeBoardService.findById(knowledgeDataId));
	}

	@DeleteMapping("/{knowledgeDataId}")
	public ResponseEntity<Void> delete(
		@AuthenticationPrincipal Long actorUserId,
		@PathVariable Long knowledgeDataId
	) {
		knowledgeBoardService.delete(actorUserId, knowledgeDataId);
		return ResponseEntity.noContent().build();
	}
}
