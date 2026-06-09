package com.wip.workipedia.ticket.controller;

import com.wip.workipedia.common.request.BasePageRequest;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.ticket.domain.TicketStatus;
import com.wip.workipedia.ticket.dto.CreateTicketRequest;
import com.wip.workipedia.ticket.dto.TicketAssigneeRequest;
import com.wip.workipedia.ticket.dto.TicketAssigneeResponse;
import com.wip.workipedia.ticket.dto.TicketResponse;
import com.wip.workipedia.ticket.dto.TicketStatusRequest;
import com.wip.workipedia.ticket.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {
	private final TicketService ticketService;

	// 티켓 생성
	@PostMapping
	public ResponseEntity<TicketResponse> create(@Valid @RequestBody CreateTicketRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.create(request));
	}

	// 내 팀 티켓 목록 조회(상태별 필터링)
	@GetMapping
	public ResponseEntity<PageResponse<TicketResponse>> findAll(
			@RequestParam(required = false) TicketStatus status,
			@Valid BasePageRequest pageRequest) {
		Sort sort = Sort.by(Sort.Direction.DESC, "ticketId");
		return ResponseEntity.ok(ticketService.findMyTeamTickets(status, pageRequest.toPageable(sort)));
	}

	// 티켓 상세 조회
	@GetMapping("/{ticketId}")
	public ResponseEntity<TicketResponse> findById(@PathVariable Long ticketId) {
		return ResponseEntity.ok(ticketService.findById(ticketId));
	}

	// 티켓 상태 변경
	@PatchMapping("/{ticketId}/status")
	public ResponseEntity<TicketResponse> changeStatus(
			@PathVariable Long ticketId,
			@Valid @RequestBody TicketStatusRequest request) {
		return ResponseEntity.ok(ticketService.changeStatus(ticketId, request.status()));
	}

	// 티켓 담당자 배정
	@PatchMapping("/{ticketId}/assignee")
	public ResponseEntity<TicketAssigneeResponse> assign(
			@PathVariable Long ticketId,
			@Valid @RequestBody TicketAssigneeRequest request) {
		return ResponseEntity.ok(ticketService.assign(ticketId, request.assigneeId()));
	}
}
