package com.wip.workipedia.knowledge.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.aisync.domain.AiSyncOperation;
import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.aisync.service.AiSyncJobService;
import com.wip.workipedia.knowledge.domain.KnowledgeData;
import com.wip.workipedia.knowledge.dto.KnowledgeBoardResponse;
import com.wip.workipedia.knowledge.repository.KnowledgeDataRepository;
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
public class KnowledgeBoardService {

	private final KnowledgeDataRepository knowledgeDataRepository;
	private final UserRepository userRepository;
	private final AiSyncJobService aiSyncJobService;

	public PageResponse<KnowledgeBoardResponse> findAll(Pageable pageable) {
		return PageResponse.from(
			knowledgeDataRepository.findBoard(pageable)
				.map(KnowledgeBoardResponse::from)
		);
	}

	public KnowledgeBoardResponse findById(Long knowledgeDataId) {
		return knowledgeDataRepository.findBoardById(knowledgeDataId)
			.map(KnowledgeBoardResponse::from)
			.orElseThrow(() -> new CustomException(ErrorType.KNOWLEDGE_DATA_NOT_FOUND));
	}

	@Transactional
	public void delete(Long actorUserId, Long knowledgeDataId) {
		User actor = getKnowledgeDataDeleteActor(actorUserId);
		KnowledgeData knowledgeData = getActiveKnowledgeData(knowledgeDataId);
		assertCanDelete(actor, knowledgeData);
		knowledgeData.delete(actor.getUserId());
		aiSyncJobService.enqueue(AiSyncSourceType.KNOWLEDGE_DATA, knowledgeDataId, AiSyncOperation.DELETE);
	}

	private User getKnowledgeDataDeleteActor(Long actorUserId) {
		return userRepository.findById(actorUserId)
			.orElseThrow(() -> new CustomException(ErrorType.KNOWLEDGE_DATA_FORBIDDEN));
	}

	private KnowledgeData getActiveKnowledgeData(Long knowledgeDataId) {
		return knowledgeDataRepository.findByKnowledgeDataIdAndDeletedAtIsNull(knowledgeDataId)
			.orElseThrow(() -> new CustomException(ErrorType.KNOWLEDGE_DATA_NOT_FOUND));
	}

	private void assertCanDelete(User actor, KnowledgeData knowledgeData) {
		if (actor.getRole() == UserRole.SYSTEM_ADMIN) {
			return;
		}
		if (actor.getRole() == UserRole.TEAM_ADMIN
			&& actor.getDepartment() != null
			&& actor.getDepartment().getDepartmentId().equals(knowledgeData.getDepartmentId())) {
			return;
		}
		throw new CustomException(ErrorType.KNOWLEDGE_DATA_FORBIDDEN);
	}
}
