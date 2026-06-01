package com.wip.workipedia.ticket.repository;

import com.wip.workipedia.ticket.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
