package com.wip.workipedia.ticket.controller;

import com.wip.workipedia.common.response.ApiResponse;
import com.wip.workipedia.ticket.dto.CreateTicketRequest;
import com.wip.workipedia.ticket.dto.TicketAssigneeRequest;
import com.wip.workipedia.ticket.dto.TicketAssigneeResponse;
import com.wip.workipedia.ticket.dto.TicketResponse;
import com.wip.workipedia.ticket.dto.TicketStatusRequest;
import com.wip.workipedia.ticket.service.TicketService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {
	private final TicketService ticketService;

	@PostMapping
	public ResponseEntity<ApiResponse<TicketResponse>> create(@Valid @RequestBody CreateTicketRequest request) {
		return ApiResponse.success(HttpStatus.CREATED, "티켓 생성 완료", ticketService.create(request));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<TicketResponse>>> findAll() {
		return ApiResponse.success(HttpStatus.OK, "티켓 목록 조회 성공", ticketService.findAll());
	}

	@GetMapping("/{ticketId}")
	public ResponseEntity<ApiResponse<TicketResponse>> findById(@PathVariable Long ticketId) {
		return ApiResponse.success(HttpStatus.OK, "티켓 상세 조회 성공", ticketService.findById(ticketId));
	}

	@PatchMapping("/{ticketId}/status")
	public ResponseEntity<ApiResponse<TicketResponse>> changeStatus(
		@PathVariable Long ticketId,
		@Valid @RequestBody TicketStatusRequest request
	) {
		return ApiResponse.success(HttpStatus.OK, "티켓 상태 변경 완료", ticketService.changeStatus(ticketId, request.status()));
	}

	@PatchMapping("/{ticketId}/assignee")
	public ResponseEntity<ApiResponse<TicketAssigneeResponse>> assign(
		@PathVariable Long ticketId,
		@Valid @RequestBody TicketAssigneeRequest request
	) {
		return ApiResponse.success(HttpStatus.OK, "티켓 담당자 배정 완료", ticketService.assign(ticketId, request.assigneeId()));
	}
}
