package com.wip.workipedia.ticket.repository;

import com.wip.workipedia.ticket.domain.TicketFile;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketFileRepository extends JpaRepository<TicketFile, Long> {

	List<TicketFile> findByTicketIdAndDeletedAtIsNullOrderBySortOrderAsc(Long ticketId);

	List<TicketFile> findByTicketIdInAndDeletedAtIsNullOrderByTicketIdAscSortOrderAsc(Collection<Long> ticketIds);
}
