package com.wip.workipedia.ticket.repository;

import com.wip.workipedia.ticket.domain.RoutingDecision;
import com.wip.workipedia.ticket.domain.Ticket;
import com.wip.workipedia.ticket.domain.TicketPriority;
import com.wip.workipedia.ticket.domain.TicketStatus;
import com.wip.workipedia.ticket.domain.TicketTransferRequestStatus;
import com.wip.workipedia.ticket.domain.CommonQueueReason;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
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

	@Query("""
		SELECT t
		FROM Ticket t
		WHERE t.status = com.wip.workipedia.ticket.domain.TicketStatus.COMMON_QUEUE
		  AND t.deletedAt IS NULL
		  AND t.isDeleted = 'N'
		ORDER BY t.createdAt DESC
		""")
	Page<Ticket> findActiveCommonQueueTickets(Pageable pageable);

	Page<Ticket> findByStatusInAndDeletedAtIsNullOrderByCreatedAtDesc(Collection<TicketStatus> statuses, Pageable pageable);

	@Query(
		value = """
		SELECT
		  t.ticketId AS ticketId,
		  t.status AS status,
		  t.assignedDepartmentId AS assignedDepartmentId,
		  t.routingConfidenceScore AS routingConfidenceScore,
		  t.routingDecision AS routingDecision,
		  t.sourceChatbotMessageId AS sourceChatbotMessageId,
		  t.priority AS priority,
		  t.title AS title,
		  t.content AS content,
		  t.assigneeId AS assigneeId,
		  t.commonQueueReason AS commonQueueReason,
		  t.commonQueueEnteredAt AS commonQueueEnteredAt,
		  tr.reason AS transferReason,
		  t.createdAt AS createdAt,
		  t.updatedAt AS updatedAt
		FROM Ticket t
		LEFT JOIN TicketTransferRequest tr
		  ON tr.ticketId = t.ticketId
		 AND tr.status = :transferStatus
		 AND tr.deletedAt IS NULL
		 AND tr.isDeleted = 'N'
		 AND tr.transferRequestId = (
		   SELECT MAX(tr3.transferRequestId)
		   FROM TicketTransferRequest tr3
		   WHERE tr3.ticketId = t.ticketId
		     AND tr3.status = :transferStatus
		     AND tr3.deletedAt IS NULL
		     AND tr3.isDeleted = 'N'
		     AND tr3.createdAt = (
		       SELECT MAX(tr2.createdAt)
		       FROM TicketTransferRequest tr2
		       WHERE tr2.ticketId = t.ticketId
		         AND tr2.status = :transferStatus
		         AND tr2.deletedAt IS NULL
		         AND tr2.isDeleted = 'N'
		     )
		 )
		WHERE t.status = com.wip.workipedia.ticket.domain.TicketStatus.COMMON_QUEUE
		  AND t.deletedAt IS NULL
		  AND t.isDeleted = 'N'
		ORDER BY t.createdAt DESC
		""",
		countQuery = """
		SELECT COUNT(t)
		FROM Ticket t
		WHERE t.status = com.wip.workipedia.ticket.domain.TicketStatus.COMMON_QUEUE
		  AND t.deletedAt IS NULL
		  AND t.isDeleted = 'N'
		  AND (:transferStatus IS NULL OR :transferStatus IS NOT NULL)
		"""
	)
	Page<CommonQueueTicketProjection> findCommonQueueTickets(
		@Param("transferStatus") TicketTransferRequestStatus transferStatus,
		Pageable pageable
	);

	long countByRequesterIdAndDeletedAtIsNull(Long requesterId);

	long countByAssignedDepartmentIdAndAssignedAtGreaterThanEqualAndAssignedAtLessThanAndDeletedAtIsNull(
		Long assignedDepartmentId,
		LocalDateTime startAt,
		LocalDateTime endAt
	);

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

	@Query(
		value = """
			SELECT DATE_FORMAT(t.assigned_at, '%Y-%m') AS month, COUNT(*) AS count
			FROM tickets t
			WHERE t.assigned_department_id = :departmentId
				AND t.routing_decision = 'AUTO_ASSIGNED'
				AND t.assigned_at >= :startAt
				AND t.assigned_at < :endAt
				AND t.deleted_at IS NULL
				AND t.is_deleted = 'N'
			GROUP BY DATE_FORMAT(t.assigned_at, '%Y-%m')
			""",
		nativeQuery = true
	)
	List<MonthlyCountProjection> countMonthlyAutoAssignedByDepartment(
		@Param("departmentId") Long departmentId,
		@Param("startAt") LocalDateTime startAt,
		@Param("endAt") LocalDateTime endAt
	);

	@Query(
		value = """
			SELECT DATE_FORMAT(t.created_at, '%Y-%m') AS month, COUNT(*) AS count
			FROM tickets t
			WHERE t.created_at >= :startAt
				AND t.created_at < :endAt
				AND t.deleted_at IS NULL
				AND t.is_deleted = 'N'
			GROUP BY DATE_FORMAT(t.created_at, '%Y-%m')
			""",
		nativeQuery = true
	)
	List<MonthlyCountProjection> countMonthlyIssuedTickets(
		@Param("startAt") LocalDateTime startAt,
		@Param("endAt") LocalDateTime endAt
	);

	@Query(
		value = """
			SELECT
				DATE_FORMAT(t.created_at, '%Y-%m') AS month,
				COUNT(*) AS totalTicketCount,
				SUM(CASE WHEN t.routing_decision = 'AUTO_ASSIGNED' THEN 1 ELSE 0 END) AS autoAssignedTicketCount
			FROM tickets t
			WHERE t.created_at >= :startAt
				AND t.created_at < :endAt
				AND t.deleted_at IS NULL
				AND t.is_deleted = 'N'
			GROUP BY DATE_FORMAT(t.created_at, '%Y-%m')
			""",
		nativeQuery = true
	)
	List<MonthlyAutoAssignmentRateProjection> countMonthlyAutoAssignmentRate(
		@Param("startAt") LocalDateTime startAt,
		@Param("endAt") LocalDateTime endAt
	);

	@Query(
		value = """
			SELECT
				t.assigned_department_id AS departmentId,
				t.status AS status,
				COUNT(*) AS count
			FROM tickets t
			WHERE t.assigned_department_id IS NOT NULL
				AND t.status IN ('ASSIGNED', 'COMPLETED')
				AND t.deleted_at IS NULL
				AND t.is_deleted = 'N'
			GROUP BY t.assigned_department_id, t.status
			""",
		nativeQuery = true
	)
	List<DepartmentTicketStatusCountProjection> countActiveTicketsByDepartmentAndStatus();

	@Query(
		value = """
			SELECT
				t.assigned_department_id AS departmentId,
				COUNT(*) AS totalTicketCount,
				SUM(CASE WHEN t.routing_decision = 'AUTO_ASSIGNED' THEN 1 ELSE 0 END) AS autoAssignedTicketCount
			FROM tickets t
			WHERE t.assigned_department_id IS NOT NULL
				AND t.deleted_at IS NULL
				AND t.is_deleted = 'N'
			GROUP BY t.assigned_department_id
			""",
		nativeQuery = true
	)
	List<DepartmentAutoAssignmentRateProjection> countDepartmentAutoAssignmentRate();

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		SELECT t
		FROM Ticket t
		WHERE t.ticketId = :ticketId
		  AND t.deletedAt IS NULL
		  AND t.isDeleted = 'N'
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
            common_queue_reason = 'ASSIGNMENT_EXPIRED',
            common_queue_entered_at = NOW(),
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

	@Modifying
	@Query(
		value = """
        UPDATE tickets
        SET status = 'DELETED',
            deleted_at = NOW(),
            is_deleted = 'Y',
            updated_at = NOW()
        WHERE status = 'COMMON_QUEUE'
          AND created_at <= DATE_SUB(NOW(), INTERVAL 7 DAY)
          AND deleted_at IS NULL
          AND is_deleted = 'N'
    """,
		nativeQuery = true
	)
	int softDeleteExpiredCommonQueueTickets();

	interface TicketStatusCountProjection {
		TicketStatus getStatus();

		long getCount();
	}

	interface MonthlyCountProjection {
		String getMonth();

		long getCount();
	}

	interface MonthlyAutoAssignmentRateProjection {
		String getMonth();

		long getTotalTicketCount();

		long getAutoAssignedTicketCount();
	}

	interface DepartmentTicketStatusCountProjection {
		Long getDepartmentId();

		TicketStatus getStatus();

		long getCount();
	}

	interface DepartmentAutoAssignmentRateProjection {
		Long getDepartmentId();

		long getTotalTicketCount();

		long getAutoAssignedTicketCount();
	}

	interface CommonQueueTicketProjection {
		Long getTicketId();

		TicketStatus getStatus();

		Long getAssignedDepartmentId();

		BigDecimal getRoutingConfidenceScore();

		RoutingDecision getRoutingDecision();

		Long getSourceChatbotMessageId();

		TicketPriority getPriority();

		String getTitle();

		String getContent();

		Long getAssigneeId();

		CommonQueueReason getCommonQueueReason();

		LocalDateTime getCommonQueueEnteredAt();

		String getTransferReason();

		LocalDateTime getCreatedAt();

		LocalDateTime getUpdatedAt();
	}
}
