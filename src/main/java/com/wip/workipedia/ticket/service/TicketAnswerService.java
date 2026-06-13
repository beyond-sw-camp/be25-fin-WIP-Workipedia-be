package com.wip.workipedia.ticket.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.notification.service.NotificationService;
import com.wip.workipedia.storage.dto.StoredObjectMetadata;
import com.wip.workipedia.storage.service.StorageService;
import com.wip.workipedia.ticket.domain.Ticket;
import com.wip.workipedia.ticket.domain.TicketAnswer;
import com.wip.workipedia.ticket.domain.TicketStatus;
import com.wip.workipedia.ticket.dto.TicketAnswerCreateRequest;
import com.wip.workipedia.ticket.dto.TicketAnswerResponse;
import com.wip.workipedia.ticket.repository.TicketAnswerRepository;
import com.wip.workipedia.ticket.repository.TicketRepository;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketAnswerService {

	private static final String TICKET_REPLY_FILE_PREFIX = "tickets/replies/";

	private final TicketRepository ticketRepository;
	private final TicketAnswerRepository ticketAnswerRepository;
	private final UserRepository userRepository;
	private final NotificationService notificationService;
	private final StorageService storageService;

	@Transactional
	public TicketAnswerResponse createOfficialAnswer(Long actorUserId, Long ticketId, TicketAnswerCreateRequest request) {
		User actor = getUser(actorUserId);
		Ticket ticket = ticketRepository.findActiveByTicketIdForUpdate(ticketId)
			.orElseThrow(() -> new CustomException(ErrorType.TICKET_NOT_FOUND));
		assertAssignedDepartmentMember(actor, ticket);
		validateAnswerable(ticket);
		StoredObjectMetadata attachment = resolveAttachment(request.fileKey());

		TicketAnswer answer = ticketAnswerRepository.save(
			TicketAnswer.create(
				ticket.getTicketId(),
				actor.getUserId(),
				request.content().trim(),
				attachment == null ? null : attachment.objectKey(),
				attachment == null ? null : attachment.publicUrl(),
				attachment == null ? null : attachment.fileName(),
				attachment == null ? null : attachment.contentType(),
				attachment == null ? null : attachment.contentLength()
			)
		);
		ticket.complete();
		notificationService.createTicketNotification(ticket.getRequesterId(), ticket);
		return TicketAnswerResponse.from(answer, actor);
	}

	public TicketAnswerResponse findLatestAnswer(Long actorUserId, Long ticketId) {
		User actor = getUser(actorUserId);
		Ticket ticket = ticketRepository.findByTicketIdAndDeletedAtIsNull(ticketId)
			.orElseThrow(() -> new CustomException(ErrorType.TICKET_NOT_FOUND));
		assertAssignedDepartmentMember(actor, ticket);
		TicketAnswer answer = ticketAnswerRepository.findTopByTicketIdAndDeletedAtIsNullOrderByCreatedAtDesc(ticketId)
			.orElseThrow(() -> new CustomException(ErrorType.TICKET_NOT_FOUND, "Ticket answer not found."));
		User author = userRepository.findById(answer.getAuthorId()).orElse(null);
		return TicketAnswerResponse.from(answer, author);
	}

	private StoredObjectMetadata resolveAttachment(String objectKey) {
		if (objectKey == null || objectKey.isBlank()) {
			return null;
		}
		String normalizedKey = objectKey.trim();
		if (!normalizedKey.startsWith(TICKET_REPLY_FILE_PREFIX)) {
			throw new CustomException(ErrorType.TICKET_INVALID_ATTACHMENT);
		}
		try {
			return storageService.getObjectMetadata(normalizedKey);
		} catch (RuntimeException e) {
			throw new CustomException(ErrorType.TICKET_INVALID_ATTACHMENT);
		}
	}

	private User getUser(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorType.TICKET_FORBIDDEN));
	}

	private void assertAssignedDepartmentMember(User actor, Ticket ticket) {
		Long actorDepartmentId = actor.getDepartment().getDepartmentId();
		if (!actorDepartmentId.equals(ticket.getAssignedDepartmentId())) {
			throw new CustomException(ErrorType.TICKET_FORBIDDEN);
		}
	}

	private void validateAnswerable(Ticket ticket) {
		if (ticket.getStatus() != TicketStatus.ASSIGNED) {
			throw new CustomException(ErrorType.TICKET_INVALID_ANSWER);
		}
	}
}

