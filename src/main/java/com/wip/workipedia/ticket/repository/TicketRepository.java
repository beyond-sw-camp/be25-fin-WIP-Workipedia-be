package com.wip.workipedia.ticket.repository;

import com.wip.workipedia.ticket.domain.Ticket;
import com.wip.workipedia.ticket.domain.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

	Page<Ticket> findByStatusAndDeletedAtIsNull(TicketStatus status, Pageable pageable);

	Page<Ticket> findByAssignedDepartmentIdAndDeletedAtIsNull(Long assignedDepartmentId, Pageable pageable);

	Page<Ticket> findByStatusAndAssignedDepartmentIdAndDeletedAtIsNull(TicketStatus status, Long assignedDepartmentId, Pageable pageable);

	Page<Ticket> findByDeletedAtIsNull(Pageable pageable);

	long countByRequesterIdAndDeletedAtIsNull(Long requesterId);

	@Modifying
	@Query(
		value = """
        UPDATE tickets
        SET status = 'COMMON_QUEUE'
        WHERE status = 'ASSIGNED'
          AND assigned_at IS NOT NULL
          AND assigned_at <= DATE_SUB(NOW(), INTERVAL 48 HOUR)
          AND deleted_at IS NULL
          AND is_deleted = 'N'
    """,
		nativeQuery = true
	)
	int moveExpiredTicketsToCommonQueue();
}
