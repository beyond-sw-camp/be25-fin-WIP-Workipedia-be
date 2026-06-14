package com.wip.workipedia.ticket.repository;

import com.wip.workipedia.ticket.domain.Ticket;
import com.wip.workipedia.ticket.domain.TicketStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

	Optional<Ticket> findByTicketIdAndDeletedAtIsNull(Long ticketId);

	Page<Ticket> findByStatusAndDeletedAtIsNull(TicketStatus status, Pageable pageable);

	Page<Ticket> findByAssignedDepartmentIdAndDeletedAtIsNull(Long assignedDepartmentId, Pageable pageable);

	Page<Ticket> findByStatusAndAssignedDepartmentIdAndDeletedAtIsNull(TicketStatus status, Long assignedDepartmentId, Pageable pageable);

	@Query("""
		SELECT t
		FROM Ticket t
		WHERE t.assignedDepartmentId = :departmentId
		  AND t.deletedAt IS NULL
		  AND (:status IS NULL OR t.status = :status)
		  AND (
		    t.status <> com.wip.workipedia.ticket.domain.TicketStatus.COMPLETED
		    OR t.completedAt >= :completedVisibleAfter
		  )
		""")
	Page<Ticket> findVisibleTeamTickets(
		@Param("departmentId") Long departmentId,
		@Param("status") TicketStatus status,
		@Param("completedVisibleAfter") LocalDateTime completedVisibleAfter,
		Pageable pageable
	);

	Page<Ticket> findByDeletedAtIsNull(Pageable pageable);

	Page<Ticket> findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(TicketStatus status, Pageable pageable);

	Page<Ticket> findByStatusInAndDeletedAtIsNullOrderByCreatedAtDesc(Collection<TicketStatus> statuses, Pageable pageable);

	long countByRequesterIdAndDeletedAtIsNull(Long requesterId);

	@Query("""
		SELECT t.status AS status, COUNT(t) AS count
		FROM Ticket t
		WHERE t.assignedDepartmentId = :departmentId
		  AND t.deletedAt IS NULL
		  AND t.status IN :statuses
		GROUP BY t.status
		""")
	List<TicketStatusCountProjection> countByStatusInDepartment(
		@Param("departmentId") Long departmentId,
		@Param("statuses") Collection<TicketStatus> statuses
	);

	@Query("""
		SELECT t.status AS status, COUNT(t) AS count
		FROM Ticket t
		WHERE t.assignedDepartmentId = :departmentId
		  AND t.deletedAt IS NULL
		  AND t.status IN :statuses
		  AND (
		    t.status <> com.wip.workipedia.ticket.domain.TicketStatus.COMPLETED
		    OR t.completedAt >= :completedVisibleAfter
		  )
		GROUP BY t.status
		""")
	List<TicketStatusCountProjection> countVisibleByStatusInDepartment(
		@Param("departmentId") Long departmentId,
		@Param("statuses") Collection<TicketStatus> statuses,
		@Param("completedVisibleAfter") LocalDateTime completedVisibleAfter
	);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		SELECT t
		FROM Ticket t
		WHERE t.ticketId = :ticketId
		  AND t.deletedAt IS NULL
		""")
	Optional<Ticket> findActiveByTicketIdForUpdate(@Param("ticketId") Long ticketId);

	@Modifying
	@Query(
		value = """
        UPDATE tickets
        SET status = 'COMMON_QUEUE',
            assignee_id = NULL,
            assigned_department_id = NULL,
            assigned_at = NULL,
            routing_decision = 'COMMON_QUEUE',
            updated_at = NOW()
        WHERE status = 'ASSIGNED'
          AND assigned_at IS NOT NULL
          AND assigned_at <= DATE_SUB(NOW(), INTERVAL 48 HOUR)
          AND deleted_at IS NULL
          AND is_deleted = 'N'
    """,
		nativeQuery = true
	)
	int moveExpiredTicketsToCommonQueue();

	interface TicketStatusCountProjection {
		TicketStatus getStatus();

		long getCount();
	}
}
