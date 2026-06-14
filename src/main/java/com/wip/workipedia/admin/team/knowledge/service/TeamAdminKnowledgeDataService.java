package com.wip.workipedia.admin.team.knowledge.service;

import com.wip.workipedia.admin.team.knowledge.dto.KnowledgeDataApprovalRequest;
import com.wip.workipedia.admin.team.knowledge.dto.KnowledgeDataResponse;
import com.wip.workipedia.admin.team.knowledge.dto.KnowledgeDataUpdateRequest;
import com.wip.workipedia.admin.team.knowledge.dto.KnowledgeTicketCandidateResponse;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.knowledge.domain.KnowledgeData;
import com.wip.workipedia.knowledge.repository.KnowledgeDataRepository;
import com.wip.workipedia.ticket.domain.KnowledgeReviewStatus;
import com.wip.workipedia.ticket.domain.Ticket;
import com.wip.workipedia.ticket.domain.TicketStatus;
import com.wip.workipedia.ticket.repository.TicketAnswerRepository;
import com.wip.workipedia.ticket.repository.TicketRepository;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.domain.UserRole;
import com.wip.workipedia.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamAdminKnowledgeDataService {

	private final KnowledgeDataRepository knowledgeDataRepository;
	private final TicketRepository ticketRepository;
	private final TicketAnswerRepository ticketAnswerRepository;
	private final UserRepository userRepository;

	public PageResponse<KnowledgeTicketCandidateResponse> findApprovalCandidates(Long actorUserId, Pageable pageable) {
		User actor = getTeamAdmin(actorUserId);
		Long departmentId = actor.getDepartment().getDepartmentId();
		return PageResponse.from(
			knowledgeDataRepository.findApprovalCandidates(departmentId, pageable)
				.map(this::toCandidateResponse)
		);
	}

	public PageResponse<KnowledgeDataResponse> findApproved(Long actorUserId, Pageable pageable) {
		User actor = getTeamAdmin(actorUserId);
		Long departmentId = actor.getDepartment().getDepartmentId();
		return PageResponse.from(
			knowledgeDataRepository.findByDepartmentIdAndDeletedAtIsNull(departmentId, pageable)
				.map(KnowledgeDataResponse::from)
		);
	}

	@Transactional
	public KnowledgeDataResponse approve(Long actorUserId, Long ticketId, KnowledgeDataApprovalRequest request) {
		User actor = getTeamAdmin(actorUserId);
		Ticket ticket = ticketRepository.findActiveByTicketIdForUpdate(ticketId)
			.orElseThrow(() -> new CustomException(ErrorType.TICKET_NOT_FOUND));
		assertDepartmentTicket(actor, ticket);
		assertCompleted(ticket);
		assertReviewPending(ticket);
		if (knowledgeDataRepository.existsByTicketId(ticket.getTicketId())) {
			throw new CustomException(ErrorType.KNOWLEDGE_DATA_ALREADY_APPROVED);
		}
		ticketAnswerRepository.findTopByTicketIdAndDeletedAtIsNullOrderByCreatedAtDesc(ticket.getTicketId())
			.orElseThrow(() -> new CustomException(ErrorType.KNOWLEDGE_DATA_INVALID_APPROVAL));

		KnowledgeData knowledgeData = KnowledgeData.approve(
			ticket.getTicketId(),
			request.question().trim(),
			request.answer().trim(),
			ticket.getAssignedDepartmentId(),
			actor.getUserId()
		);
		ticket.approveKnowledgeReview(actor.getUserId());
		return KnowledgeDataResponse.from(knowledgeDataRepository.save(knowledgeData));
	}

	@Transactional
	public void reject(Long actorUserId, Long ticketId) {
		User actor = getTeamAdmin(actorUserId);
		Ticket ticket = ticketRepository.findActiveByTicketIdForUpdate(ticketId)
			.orElseThrow(() -> new CustomException(ErrorType.TICKET_NOT_FOUND));
		assertDepartmentTicket(actor, ticket);
		assertCompleted(ticket);
		assertReviewPending(ticket);
		if (knowledgeDataRepository.existsByTicketId(ticket.getTicketId())) {
			throw new CustomException(ErrorType.KNOWLEDGE_DATA_ALREADY_APPROVED);
		}
		ticket.rejectKnowledgeReview(actor.getUserId());
	}

	@Transactional
	public KnowledgeDataResponse update(Long actorUserId, Long knowledgeDataId, KnowledgeDataUpdateRequest request) {
		User actor = getTeamAdmin(actorUserId);
		KnowledgeData knowledgeData = getActiveKnowledgeData(knowledgeDataId);
		assertDepartmentKnowledgeData(actor, knowledgeData);
		knowledgeData.update(request.question().trim(), request.answer().trim(), actor.getUserId());
		return KnowledgeDataResponse.from(knowledgeData);
	}

	@Transactional
	public void delete(Long actorUserId, Long knowledgeDataId) {
		User actor = getTeamAdmin(actorUserId);
		KnowledgeData knowledgeData = getActiveKnowledgeData(knowledgeDataId);
		assertDepartmentKnowledgeData(actor, knowledgeData);
		knowledgeData.delete(actor.getUserId());
	}

	private User getTeamAdmin(Long actorUserId) {
		User user = userRepository.findById(actorUserId)
			.orElseThrow(() -> new CustomException(ErrorType.KNOWLEDGE_DATA_FORBIDDEN));
		if (user.getRole() != UserRole.TEAM_ADMIN || user.getDepartment() == null) {
			throw new CustomException(ErrorType.KNOWLEDGE_DATA_FORBIDDEN);
		}
		return user;
	}

	private KnowledgeData getActiveKnowledgeData(Long knowledgeDataId) {
		return knowledgeDataRepository.findByKnowledgeDataIdAndDeletedAtIsNull(knowledgeDataId)
			.orElseThrow(() -> new CustomException(ErrorType.KNOWLEDGE_DATA_NOT_FOUND));
	}

	private void assertDepartmentTicket(User actor, Ticket ticket) {
		Long actorDepartmentId = actor.getDepartment().getDepartmentId();
		if (!actorDepartmentId.equals(ticket.getAssignedDepartmentId())) {
			throw new CustomException(ErrorType.KNOWLEDGE_DATA_FORBIDDEN);
		}
	}

	private void assertDepartmentKnowledgeData(User actor, KnowledgeData knowledgeData) {
		Long actorDepartmentId = actor.getDepartment().getDepartmentId();
		if (!actorDepartmentId.equals(knowledgeData.getDepartmentId())) {
			throw new CustomException(ErrorType.KNOWLEDGE_DATA_FORBIDDEN);
		}
	}

	private void assertCompleted(Ticket ticket) {
		if (ticket.getStatus() != TicketStatus.COMPLETED) {
			throw new CustomException(ErrorType.KNOWLEDGE_DATA_INVALID_APPROVAL);
		}
	}

	private void assertReviewPending(Ticket ticket) {
		if (ticket.getKnowledgeReviewStatus() != null && ticket.getKnowledgeReviewStatus() != KnowledgeReviewStatus.PENDING) {
			throw new CustomException(ErrorType.KNOWLEDGE_DATA_INVALID_APPROVAL);
		}
	}

	private KnowledgeTicketCandidateResponse toCandidateResponse(
		KnowledgeDataRepository.KnowledgeTicketCandidateProjection projection
	) {
		return new KnowledgeTicketCandidateResponse(
			projection.getTicketId(),
			projection.getDepartmentId(),
			projection.getQuestion(),
			projection.getAnswer(),
			projection.getAnswerId(),
			projection.getAnswerAuthorId(),
			projection.getCompletedAt(),
			projection.getAnsweredAt()
		);
	}
}
