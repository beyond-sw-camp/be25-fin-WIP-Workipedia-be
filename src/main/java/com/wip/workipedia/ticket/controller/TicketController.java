package com.wip.workipedia.ticket.controller;

import com.wip.workipedia.common.request.BasePageRequest;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.ticket.domain.TicketStatus;
import com.wip.workipedia.ticket.dto.CreateTicketRequest;
import com.wip.workipedia.ticket.dto.TicketAnswerCreateRequest;
import com.wip.workipedia.ticket.dto.TicketAnswerResponse;
import com.wip.workipedia.ticket.dto.TicketAssigneeRequest;
import com.wip.workipedia.ticket.dto.TicketAssigneeResponse;
import com.wip.workipedia.ticket.dto.TicketResponse;
import com.wip.workipedia.ticket.dto.TicketStatusRequest;
import com.wip.workipedia.ticket.service.TicketAnswerService;
import com.wip.workipedia.ticket.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {
	private final TicketService ticketService;
	private final TicketAnswerService ticketAnswerService;

	// 티켓 생성
	@PostMapping
	public ResponseEntity<TicketResponse> create(
			@AuthenticationPrincipal Long userId,
			@Valid @RequestBody CreateTicketRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.create(userId, request));
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<TicketResponse> createWithFiles(
			@AuthenticationPrincipal Long userId,
			@RequestParam(required = false) Long sourceChatbotMessageId,
			@RequestParam(required = false) com.wip.workipedia.ticket.domain.TicketPriority priority,
			@RequestParam String title,
			@RequestParam String content,
			@RequestParam(name = "file", required = false) List<MultipartFile> file,
			@RequestParam(name = "files", required = false) List<MultipartFile> files,
			@RequestParam(name = "image", required = false) List<MultipartFile> image,
			@RequestParam(name = "images", required = false) List<MultipartFile> images,
			@RequestParam(name = "file[]", required = false) List<MultipartFile> fileArray) {
		CreateTicketRequest request = new CreateTicketRequest(sourceChatbotMessageId, priority, title, content);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ticketService.create(userId, request, resolveFiles(file, files, image, images, fileArray)));
	}

	// 내 팀 티켓 목록 조회(상태별 필터링)
	@GetMapping
	public ResponseEntity<PageResponse<TicketResponse>> findAll(
			@AuthenticationPrincipal Long userId,
			@RequestParam(required = false) TicketStatus status,
			@Valid BasePageRequest pageRequest) {
		Sort sort = Sort.by(Sort.Direction.DESC, "ticketId");
		return ResponseEntity.ok(ticketService.findMyTeamTickets(userId, status, pageRequest.toPageable(sort)));
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

	@PostMapping("/{ticketId}/answers")
	public ResponseEntity<TicketAnswerResponse> createAnswer(
			@AuthenticationPrincipal Long userId,
			@PathVariable Long ticketId,
			@Valid @RequestBody TicketAnswerCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ticketAnswerService.createOfficialAnswer(userId, ticketId, request));
	}

	@GetMapping("/{ticketId}/answers/latest")
	public ResponseEntity<TicketAnswerResponse> latestAnswer(
			@AuthenticationPrincipal Long userId,
			@PathVariable Long ticketId) {
		return ResponseEntity.ok(ticketAnswerService.findLatestAnswer(userId, ticketId));
	}

	@SafeVarargs
	private final List<MultipartFile> resolveFiles(List<MultipartFile>... fileGroups) {
		return Stream.of(fileGroups)
				.filter(group -> group != null && !group.isEmpty())
				.flatMap(List::stream)
				.toList();
	}
}
