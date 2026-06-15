package com.wip.workipedia.ticket.repository;

import com.wip.workipedia.ticket.domain.TicketRoutingLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRoutingLogRepository extends JpaRepository<TicketRoutingLog, Long> {
}
