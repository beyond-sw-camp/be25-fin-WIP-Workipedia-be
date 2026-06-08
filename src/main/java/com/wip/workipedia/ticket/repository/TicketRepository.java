package com.wip.workipedia.ticket.repository;

import com.wip.workipedia.ticket.domain.Ticket;
import com.wip.workipedia.ticket.domain.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
	
	Page<Ticket> findByStatusAndDeletedAtIsNull(TicketStatus status, Pageable pageable);

	Page<Ticket> findByAssignedDepartmentIdAndDeletedAtIsNull(Long assignedDepartmentId, Pageable pageable);

	Page<Ticket> findByStatusAndAssignedDepartmentIdAndDeletedAtIsNull(TicketStatus status, Long assignedDepartmentId, Pageable pageable);

	Page<Ticket> findByDeletedAtIsNull(Pageable pageable);

	// 마이페이지 조회 시 사용자가 발행한 티켓 수를 조회합니다.
	long countByRequesterIdAndDeletedAtIsNull(Long requesterId);
}
