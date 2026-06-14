package com.wip.workipedia.admin.team.knowledge.controller;

import com.wip.workipedia.admin.team.knowledge.dto.KnowledgeDataApprovalRequest;
import com.wip.workipedia.admin.team.knowledge.dto.KnowledgeDataResponse;
import com.wip.workipedia.admin.team.knowledge.dto.KnowledgeDataUpdateRequest;
import com.wip.workipedia.admin.team.knowledge.dto.KnowledgeTicketCandidateResponse;
import com.wip.workipedia.admin.team.knowledge.service.TeamAdminKnowledgeDataService;
import com.wip.workipedia.common.request.BasePageRequest;
import com.wip.workipedia.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/team/knowledge-data")
@RequiredArgsConstructor
public class TeamAdminKnowledgeDataController {

	private final TeamAdminKnowledgeDataService teamAdminKnowledgeDataService;

	@GetMapping("/candidates")
	public ResponseEntity<PageResponse<KnowledgeTicketCandidateResponse>> findApprovalCandidates(
		@AuthenticationPrincipal Long actorUserId,
		@Valid BasePageRequest pageRequest
	) {
		return ResponseEntity.ok(teamAdminKnowledgeDataService.findApprovalCandidates(actorUserId, pageRequest.toPageable()));
	}

	@GetMapping
	public ResponseEntity<PageResponse<KnowledgeDataResponse>> findApproved(
		@AuthenticationPrincipal Long actorUserId,
		@Valid BasePageRequest pageRequest
	) {
		Sort sort = Sort.by(Sort.Direction.DESC, "approvedAt");
		return ResponseEntity.ok(teamAdminKnowledgeDataService.findApproved(actorUserId, pageRequest.toPageable(sort)));
	}

	@PostMapping("/tickets/{ticketId}/approve")
	public ResponseEntity<KnowledgeDataResponse> approve(
		@AuthenticationPrincipal Long actorUserId,
		@PathVariable Long ticketId,
		@Valid @RequestBody KnowledgeDataApprovalRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(teamAdminKnowledgeDataService.approve(actorUserId, ticketId, request));
	}

	@PostMapping("/tickets/{ticketId}/reject")
	public ResponseEntity<Void> reject(
		@AuthenticationPrincipal Long actorUserId,
		@PathVariable Long ticketId
	) {
		teamAdminKnowledgeDataService.reject(actorUserId, ticketId);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{knowledgeDataId}")
	public ResponseEntity<KnowledgeDataResponse> update(
		@AuthenticationPrincipal Long actorUserId,
		@PathVariable Long knowledgeDataId,
		@Valid @RequestBody KnowledgeDataUpdateRequest request
	) {
		return ResponseEntity.ok(teamAdminKnowledgeDataService.update(actorUserId, knowledgeDataId, request));
	}

	@DeleteMapping("/{knowledgeDataId}")
	public ResponseEntity<Void> delete(
		@AuthenticationPrincipal Long actorUserId,
		@PathVariable Long knowledgeDataId
	) {
		teamAdminKnowledgeDataService.delete(actorUserId, knowledgeDataId);
		return ResponseEntity.noContent().build();
	}
}
