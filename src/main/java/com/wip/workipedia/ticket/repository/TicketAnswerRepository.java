package com.wip.workipedia.ticket.repository;

import com.wip.workipedia.ticket.domain.TicketAnswer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketAnswerRepository extends JpaRepository<TicketAnswer, Long> {

	Optional<TicketAnswer> findTopByTicketIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long ticketId);
}
