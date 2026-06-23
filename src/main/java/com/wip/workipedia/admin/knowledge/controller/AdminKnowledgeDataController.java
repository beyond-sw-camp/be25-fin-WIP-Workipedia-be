package com.wip.workipedia.admin.knowledge.controller;

import com.wip.workipedia.knowledge.service.KnowledgeBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/knowledge-data")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
@RequiredArgsConstructor
public class AdminKnowledgeDataController {

	private final KnowledgeBoardService knowledgeBoardService;

	@DeleteMapping("/{knowledgeDataId}")
	public ResponseEntity<Void> delete(
		@AuthenticationPrincipal Long actorUserId,
		@PathVariable Long knowledgeDataId
	) {
		knowledgeBoardService.delete(actorUserId, knowledgeDataId);
		return ResponseEntity.noContent().build();
	}
}
