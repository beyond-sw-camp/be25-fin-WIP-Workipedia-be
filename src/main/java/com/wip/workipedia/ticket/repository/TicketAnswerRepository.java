package com.wip.workipedia.ticket.repository;

import com.wip.workipedia.ticket.domain.TicketAnswer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketAnswerRepository extends JpaRepository<TicketAnswer, Long> {

	Optional<TicketAnswer> findTopByTicketIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long ticketId);

	@Query(
		value = """
			SELECT COUNT(DISTINCT ta.ticket_id)
			FROM ticket_answers ta
			JOIN tickets t ON t.ticket_id = ta.ticket_id
			WHERE ta.author_id = :authorId
				AND ta.deleted_at IS NULL
				AND t.assigned_department_id = :departmentId
				AND t.deleted_at IS NULL
				AND t.status = 'COMPLETED'
				AND t.completed_at >= DATE_SUB(NOW(), INTERVAL 48 HOUR)
			""",
		nativeQuery = true
	)
	long countVisibleAnsweredTicketsByAuthorInDepartment(
		@Param("authorId") Long authorId,
		@Param("departmentId") Long departmentId
	);
}
