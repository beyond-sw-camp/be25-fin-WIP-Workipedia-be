package com.wip.workipedia.admin.commonqueue.controller;

import com.wip.workipedia.admin.commonqueue.dto.CommonQueueAssignDepartmentRequest;
import com.wip.workipedia.admin.commonqueue.service.AdminCommonQueueService;
import com.wip.workipedia.common.request.BasePageRequest;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.ticket.dto.TicketResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/common-queue/tickets")
@RequiredArgsConstructor
public class AdminCommonQueueController {

	private final AdminCommonQueueService adminCommonQueueService;

	@GetMapping
	public ResponseEntity<PageResponse<TicketResponse>> list(@Valid BasePageRequest pageRequest) {
		Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
		return ResponseEntity.ok(adminCommonQueueService.findCommonQueueTickets(pageRequest.toPageable(sort)));
	}

	@PatchMapping("/{ticketId}/department")
	public ResponseEntity<TicketResponse> assignDepartment(
		@PathVariable Long ticketId,
		@Valid @RequestBody CommonQueueAssignDepartmentRequest request
	) {
		return ResponseEntity.ok(adminCommonQueueService.assignDepartment(ticketId, request));
	}
}
