package com.wip.workipedia.ticket.repository;

import com.wip.workipedia.ticket.domain.TicketTransferRequest;
import com.wip.workipedia.ticket.domain.TicketTransferRequestStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketTransferRequestRepository extends JpaRepository<TicketTransferRequest, Long> {

	Optional<TicketTransferRequest> findFirstByTicketIdAndStatusAndDeletedAtIsNullOrderByCreatedAtDesc(
		Long ticketId,
		TicketTransferRequestStatus status
	);
}
