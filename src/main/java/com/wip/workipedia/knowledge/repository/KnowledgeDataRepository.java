package com.wip.workipedia.knowledge.repository;

import com.wip.workipedia.knowledge.domain.KnowledgeData;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KnowledgeDataRepository extends JpaRepository<KnowledgeData, Long> {

	boolean existsByTicketId(Long ticketId);

	Optional<KnowledgeData> findByKnowledgeDataIdAndDeletedAtIsNull(Long knowledgeDataId);

	Page<KnowledgeData> findByDepartmentIdAndDeletedAtIsNull(Long departmentId, Pageable pageable);

	Optional<KnowledgeData> findByKnowledgeDataIdAndDeletedAtIsNullAndIsDeleted(Long knowledgeDataId, String isDeleted);

	Page<KnowledgeData> findByDeletedAtIsNullAndIsDeleted(String isDeleted, Pageable pageable);

	@Query(
		value = """
			SELECT
				t.ticket_id AS ticketId,
				t.assigned_department_id AS departmentId,
				t.title AS question,
				ta.content AS answer,
				ta.ticket_answer_id AS answerId,
				ta.author_id AS answerAuthorId,
				t.completed_at AS completedAt,
				ta.created_at AS answeredAt
			FROM tickets t
			JOIN ticket_answers ta
				ON ta.ticket_id = t.ticket_id
				AND ta.deleted_at IS NULL
				AND ta.ticket_answer_id = (
					SELECT latest_ta.ticket_answer_id
					FROM ticket_answers latest_ta
					WHERE latest_ta.ticket_id = t.ticket_id
						AND latest_ta.deleted_at IS NULL
					ORDER BY latest_ta.created_at DESC
					LIMIT 1
				)
			WHERE t.assigned_department_id = :departmentId
				AND t.status = 'COMPLETED'
				AND t.completed_at >= DATE_SUB(NOW(), INTERVAL 48 HOUR)
				AND (t.knowledge_review_status IS NULL OR t.knowledge_review_status = 'PENDING')
				AND t.deleted_at IS NULL
				AND NOT EXISTS (
					SELECT 1
					FROM knowledge_data kd
					WHERE kd.ticket_id = t.ticket_id
				)
			ORDER BY t.completed_at DESC, t.ticket_id DESC
			""",
		countQuery = """
			SELECT COUNT(*)
			FROM tickets t
			JOIN ticket_answers ta
				ON ta.ticket_id = t.ticket_id
				AND ta.deleted_at IS NULL
				AND ta.ticket_answer_id = (
					SELECT latest_ta.ticket_answer_id
					FROM ticket_answers latest_ta
					WHERE latest_ta.ticket_id = t.ticket_id
						AND latest_ta.deleted_at IS NULL
					ORDER BY latest_ta.created_at DESC
					LIMIT 1
				)
			WHERE t.assigned_department_id = :departmentId
				AND t.status = 'COMPLETED'
				AND t.completed_at >= DATE_SUB(NOW(), INTERVAL 48 HOUR)
				AND (t.knowledge_review_status IS NULL OR t.knowledge_review_status = 'PENDING')
				AND t.deleted_at IS NULL
				AND NOT EXISTS (
					SELECT 1
					FROM knowledge_data kd
					WHERE kd.ticket_id = t.ticket_id
				)
			""",
		nativeQuery = true
	)
	Page<KnowledgeTicketCandidateProjection> findApprovalCandidates(
		@Param("departmentId") Long departmentId,
		Pageable pageable
	);

	interface KnowledgeTicketCandidateProjection {
		Long getTicketId();

		Long getDepartmentId();

		String getQuestion();

		String getAnswer();

		Long getAnswerId();

		Long getAnswerAuthorId();

		LocalDateTime getCompletedAt();

		LocalDateTime getAnsweredAt();
	}
}
