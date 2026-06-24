package com.wip.workipedia.ticket.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.notification.service.NotificationService;
import com.wip.workipedia.storage.dto.StoredObject;
import com.wip.workipedia.storage.service.StorageService;
import com.wip.workipedia.ticket.domain.Ticket;
import com.wip.workipedia.ticket.domain.TicketFile;
import com.wip.workipedia.ticket.domain.TicketPriority;
import com.wip.workipedia.ticket.domain.TicketRoutingLog;
import com.wip.workipedia.ticket.domain.TicketStatus;
import com.wip.workipedia.ticket.dto.CreateTicketRequest;
import com.wip.workipedia.ticket.dto.RoutingResult;
import com.wip.workipedia.ticket.dto.TicketAssigneeResponse;
import com.wip.workipedia.ticket.dto.TicketFileResponse;
import com.wip.workipedia.ticket.dto.TicketResponse;
import com.wip.workipedia.ticket.repository.TicketFileRepository;
import com.wip.workipedia.ticket.repository.TicketRepository;
import com.wip.workipedia.ticket.repository.TicketRoutingLogRepository;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.repository.UserRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {
	private static final int COMMON_QUEUE_EXPIRATION_DAYS = 7;
	private static final int COMMON_QUEUE_EXPIRATION_BATCH_SIZE = 500;
	private static final String TICKET_REQUEST_FILE_PREFIX = "tickets/requests";
	private static final List<String> ALLOWED_IMAGE_CONTENT_TYPES = List.of("image/jpeg", "image/png");

	private final TicketRepository ticketRepository;
	private final TicketFileRepository ticketFileRepository;
	private final TicketRoutingService ticketRoutingService;
	private final TicketRoutingLogRepository ticketRoutingLogRepository;
	private final UserRepository userRepository;
	private final NotificationService notificationService;
	private final StorageService storageService;
	private final ObjectMapper objectMapper;

	public TicketResponse create(Long requesterId, CreateTicketRequest request) {
		log.info("[티켓생성] 요청 수신 requesterId={}, title={}", requesterId, request.title());
		RoutingResult routingResult = ticketRoutingService.route(request);
		log.info("[티켓생성] 라우팅 결과 decision={}, 부서명={}, confidence={}",
			routingResult.decision(), routingResult.assignedDepartmentName(), routingResult.confidenceScore());
		return saveTicket(requesterId, request, routingResult);
	}

	public TicketResponse create(Long requesterId, CreateTicketRequest request, List<MultipartFile> files) {
		log.info("[티켓생성] 첨부 포함 요청 수신 requesterId={}, title={}, fileCount={}",
			requesterId, request.title(), files == null ? 0 : files.size());
		RoutingResult routingResult = ticketRoutingService.route(request);
		log.info("[티켓생성] 라우팅 결과 decision={}, 부서명={}, confidence={}",
			routingResult.decision(), routingResult.assignedDepartmentName(), routingResult.confidenceScore());
		return saveTicket(requesterId, request, routingResult, files);
	}

	@Transactional
	public TicketResponse saveTicket(Long requesterId, CreateTicketRequest request, RoutingResult routingResult) {
		Ticket ticket = Ticket.create(
			requesterId,
			request.sourceChatbotMessageId(),
			defaultPriority(request.priority()),
			request.title(),
			request.content());
		ticket.applyRouting(
			routingResult.assignedDepartmentId(),
			routingResult.assignedDepartmentName(),
			routingResult.confidenceScore(),
			routingResult.decision());
		Ticket saved = ticketRepository.save(ticket);
		saveRoutingLog(saved, routingResult);
		notificationService.createTicketNotification(requesterId, saved);
		return TicketResponse.from(saved, routingResult);
	}

	@Transactional
	public TicketResponse saveTicket(
		Long requesterId,
		CreateTicketRequest request,
		RoutingResult routingResult,
		List<MultipartFile> files
	) {
		Ticket ticket = Ticket.create(
			requesterId,
			request.sourceChatbotMessageId(),
			defaultPriority(request.priority()),
			request.title(),
			request.content());
		ticket.applyRouting(
			routingResult.assignedDepartmentId(),
			routingResult.assignedDepartmentName(),
			routingResult.confidenceScore(),
			routingResult.decision());
		Ticket saved = ticketRepository.save(ticket);
		List<TicketFileResponse> attachments = saveTicketFiles(saved.getTicketId(), files);
		saveRoutingLog(saved, routingResult);
		notificationService.createTicketNotification(requesterId, saved);
		return TicketResponse.from(saved, routingResult, attachments);
	}

	@Transactional(readOnly = true)
	public PageResponse<TicketResponse> findMyTeamTickets(Long requesterId, TicketStatus status, Pageable pageable) {
		User requester = userRepository.findById(requesterId)
			.orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다. id=" + requesterId));
		Long departmentId = requester.getDepartment().getDepartmentId();
		Page<Ticket> tickets = findTickets(status, departmentId, pageable);
		Map<Long, List<TicketFileResponse>> filesByTicketId = findFilesByTicketId(tickets.getContent());
		return PageResponse.from(tickets.map(ticket ->
			TicketResponse.from(ticket, emptyRoutingResult(), filesByTicketId.getOrDefault(ticket.getTicketId(), List.of()))));
	}

	@Transactional(readOnly = true)
	public TicketResponse findById(Long ticketId) {
		Ticket ticket = getTicket(ticketId);
		return TicketResponse.from(ticket, emptyRoutingResult(), findFileResponses(ticket.getTicketId()));
	}

	@Transactional
	public TicketResponse changeStatus(Long ticketId, TicketStatus status) {
		Ticket ticket = getTicket(ticketId);
		TicketStatus previousStatus = ticket.getStatus();
		ticket.changeStatus(status);
		Ticket saved = ticketRepository.save(ticket);
		if (previousStatus != status) {
			notificationService.createTicketNotification(saved.getRequesterId(), saved);
		}
		return TicketResponse.from(saved, emptyRoutingResult(), findFileResponses(saved.getTicketId()));
	}

	@Transactional
	public TicketAssigneeResponse assign(Long ticketId, Long assigneeId) {
		Ticket ticket = getTicket(ticketId);
		ticket.assignTo(assigneeId);
		ticketRepository.save(ticket);
		User assignee = userRepository.findById(assigneeId)
			.orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다. id=" + assigneeId));
		return new TicketAssigneeResponse(
			ticket.getTicketId(),
			ticket.getStatus(),
			ticket.getPriority(),
			ticket.getAssigneeId(),
			assignee.getNickname());
	}

	private void saveRoutingLog(Ticket ticket, RoutingResult result) {
		TicketRoutingLog routingLog = TicketRoutingLog.create(
			ticket.getTicketId(),
			result.decision(),
			result.confidenceScore(),
			result.scoreMargin(),
			toJson(result.candidateDepartments()),
			toJson(result.reasons()),
			result.modelVersion()
		);
		ticketRoutingLogRepository.save(routingLog);
	}

	private List<TicketFileResponse> saveTicketFiles(Long ticketId, List<MultipartFile> files) {
		List<MultipartFile> validFiles = normalizeFiles(files);
		if (validFiles.isEmpty()) {
			return List.of();
		}
		List<TicketFileResponse> responses = new ArrayList<>();
		for (int index = 0; index < validFiles.size(); index++) {
			MultipartFile file = validFiles.get(index);
			validateTicketImage(file);
			StoredObject storedObject = uploadTicketImage(file);
			TicketFile savedFile = ticketFileRepository.save(TicketFile.create(
				ticketId,
				storedObject.objectKey(),
				storedObject.publicUrl(),
				file.getOriginalFilename(),
				normalizeContentType(file.getContentType()),
				file.getSize(),
				index + 1
			));
			responses.add(TicketFileResponse.from(savedFile));
		}
		return responses;
	}

	private List<MultipartFile> normalizeFiles(List<MultipartFile> files) {
		if (files == null || files.isEmpty()) {
			return List.of();
		}
		return files.stream()
			.filter(file -> file != null && !file.isEmpty())
			.toList();
	}

	private void validateTicketImage(MultipartFile file) {
		String contentType = normalizeContentType(file.getContentType());
		String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
		boolean allowedContentType = ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType);
		boolean allowedExtension = filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png");
		if (!allowedContentType || !allowedExtension) {
			throw new CustomException(ErrorType.TICKET_INVALID_ATTACHMENT, "티켓 첨부파일은 JPG, JPEG, PNG 이미지만 업로드할 수 있습니다.");
		}
	}

	private StoredObject uploadTicketImage(MultipartFile file) {
		try {
			return storageService.upload(
				file.getBytes(),
				TICKET_REQUEST_FILE_PREFIX,
				file.getOriginalFilename(),
				normalizeContentType(file.getContentType())
			);
		} catch (IOException e) {
			throw new CustomException(ErrorType.TICKET_INVALID_ATTACHMENT, "티켓 첨부파일을 읽을 수 없습니다.");
		} catch (RuntimeException e) {
			log.warn("티켓 첨부파일 업로드 실패. filename={}, contentType={}",
				file.getOriginalFilename(), file.getContentType(), e);
			throw new CustomException(ErrorType.TICKET_INVALID_ATTACHMENT, "티켓 첨부파일 업로드에 실패했습니다.");
		}
	}

	private String normalizeContentType(String contentType) {
		return contentType == null ? "" : contentType.toLowerCase();
	}

	private List<TicketFileResponse> findFileResponses(Long ticketId) {
		return ticketFileRepository.findByTicketIdAndDeletedAtIsNullOrderBySortOrderAsc(ticketId)
			.stream()
			.map(TicketFileResponse::from)
			.toList();
	}

	private Map<Long, List<TicketFileResponse>> findFilesByTicketId(List<Ticket> tickets) {
		if (tickets.isEmpty()) {
			return Map.of();
		}
		List<Long> ticketIds = tickets.stream()
			.map(Ticket::getTicketId)
			.toList();
		return ticketFileRepository.findByTicketIdInAndDeletedAtIsNullOrderByTicketIdAscSortOrderAsc(ticketIds)
			.stream()
			.collect(Collectors.groupingBy(
				TicketFile::getTicketId,
				Collectors.mapping(TicketFileResponse::from, Collectors.toList())
			));
	}

	private String toJson(Object value) {
		if (value == null) {
			return null;
		}
		try {
			return objectMapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			log.warn("JSON 직렬화 실패: {}", e.getMessage());
			return null;
		}
	}

	private Ticket getTicket(Long ticketId) {
		return ticketRepository.findById(ticketId)
			.orElseThrow(() -> new CustomException(ErrorType.TICKET_NOT_FOUND));
	}

	private Page<Ticket> findTickets(TicketStatus status, Long departmentId, Pageable pageable) {
		if (status != null && departmentId != null) {
			return ticketRepository.findByStatusAndAssignedDepartmentIdAndDeletedAtIsNull(status, departmentId, pageable);
		}
		if (status != null) {
			return ticketRepository.findByStatusAndDeletedAtIsNull(status, pageable);
		}
		if (departmentId != null) {
			return ticketRepository.findByAssignedDepartmentIdAndDeletedAtIsNull(departmentId, pageable);
		}
		return ticketRepository.findByDeletedAtIsNull(pageable);
	}

	private RoutingResult emptyRoutingResult() {
		return new RoutingResult(null, null, null, null, null, null, List.of(), List.of());
	}

	private TicketPriority defaultPriority(TicketPriority priority) {
		return priority == null ? TicketPriority.MEDIUM : priority;
	}

	@Transactional
	public void moveExpiredTicketsToCommonQueue() {
		ticketRepository.moveExpiredTicketsToCommonQueue();
		LocalDateTime expiredBefore = LocalDateTime.now().minusDays(COMMON_QUEUE_EXPIRATION_DAYS);
		List<TicketRepository.ExpiredCommonQueueTicketProjection> expiredTickets;
		do {
			expiredTickets = ticketRepository.findExpiredCommonQueueTickets(
					expiredBefore,
					PageRequest.of(0, COMMON_QUEUE_EXPIRATION_BATCH_SIZE)
			);
			expiredTickets.forEach(ticket -> softDeleteAndNotify(ticket, expiredBefore));
		} while (!expiredTickets.isEmpty());
	}

	private void softDeleteAndNotify(
			TicketRepository.ExpiredCommonQueueTicketProjection ticket,
			LocalDateTime expiredBefore
	) {
		int updatedRows = ticketRepository.softDeleteExpiredCommonQueueTicket(ticket.getTicketId(), expiredBefore);
		if (updatedRows != 1) {
			log.warn(
					"Skipped ticket deleted notification because expired ticket was not deleted. ticketId={}, updatedRows={}",
					ticket.getTicketId(),
					updatedRows
			);
			return;
		}
		notificationService.createTicketDeletedNotification(
				ticket.getRequesterId(),
				ticket.getTicketId(),
				ticket.getTitle()
		);
	}
}
