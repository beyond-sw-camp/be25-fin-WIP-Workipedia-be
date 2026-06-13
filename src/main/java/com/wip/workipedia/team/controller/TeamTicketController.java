package com.wip.workipedia.team.controller;

import com.wip.workipedia.common.request.BasePageRequest;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.team.dto.TeamTicketSummaryResponse;
import com.wip.workipedia.team.service.TeamTicketService;
import com.wip.workipedia.ticket.domain.TicketStatus;
import com.wip.workipedia.ticket.dto.TicketResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/team/tickets")
@RequiredArgsConstructor
public class TeamTicketController {

	private final TeamTicketService teamTicketService;

	@GetMapping("/summary")
	public ResponseEntity<TeamTicketSummaryResponse> summary(@AuthenticationPrincipal Long actorUserId) {
		return ResponseEntity.ok(teamTicketService.getSummary(actorUserId));
	}

	@GetMapping
	public ResponseEntity<PageResponse<TicketResponse>> list(
		@AuthenticationPrincipal Long actorUserId,
		@RequestParam(required = false) TicketStatus status,
		@Valid BasePageRequest pageRequest
	) {
		Sort sort = Sort.by(Sort.Direction.DESC, "ticketId");
		return ResponseEntity.ok(teamTicketService.findTickets(actorUserId, status, pageRequest.toPageable(sort)));
	}

	@GetMapping("/{ticketId}")
	public ResponseEntity<TicketResponse> detail(
		@AuthenticationPrincipal Long actorUserId,
		@PathVariable Long ticketId
	) {
		return ResponseEntity.ok(teamTicketService.findTicket(actorUserId, ticketId));
	}
}
