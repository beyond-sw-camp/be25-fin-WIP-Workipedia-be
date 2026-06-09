package com.wip.workipedia.ticket.repository;

import com.wip.workipedia.ticket.domain.Ticket;
import com.wip.workipedia.ticket.domain.TicketStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
	
	Page<Ticket> findByStatusAndDeletedAtIsNull(TicketStatus status, Pageable pageable);

	Page<Ticket> findByAssignedDepartmentIdAndDeletedAtIsNull(Long assignedDepartmentId, Pageable pageable);

	Page<Ticket> findByStatusAndAssignedDepartmentIdAndDeletedAtIsNull(TicketStatus status, Long assignedDepartmentId, Pageable pageable);

	Page<Ticket> findByDeletedAtIsNull(Pageable pageable);

	// 마이페이지 조회 시 사용자가 발행한 티켓 수를 조회합니다.
	long countByRequesterIdAndDeletedAtIsNull(Long requesterId);

	// 내 발행 티켓 목록에서 할당 부서명을 함께 보여주기 위해 departments 테이블을 조인합니다.
	@Query(
		value = """
			SELECT
				t.ticket_id AS ticketId,
				t.title AS title,
				t.assigned_department_id AS assignedDepartmentId,
				d.department_name AS assignedDepartmentName,
				t.status AS status,
				t.created_at AS createdAt
			FROM tickets t
			LEFT JOIN departments d
				ON t.assigned_department_id = d.department_id
			WHERE t.requester_id = :requesterId
				AND t.status IN (:statuses)
				AND t.deleted_at IS NULL
			ORDER BY t.created_at DESC
			""",
		countQuery = """
			SELECT COUNT(*)
			FROM tickets t
			WHERE t.requester_id = :requesterId
				AND t.status IN (:statuses)
				AND t.deleted_at IS NULL
			""",
		nativeQuery = true
	)
	Page<MyTicketProjection> findMyTickets(Long requesterId, List<String> statuses, Pageable pageable);
}
