package com.wip.workipedia.ticket.service;

import static org.mockito.Mockito.inOrder;

import com.wip.workipedia.notification.service.NotificationService;
import com.wip.workipedia.ticket.repository.TicketRepository;
import com.wip.workipedia.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.mock;

class TicketServiceTest {

	@Test
	void moveExpiredTicketsToCommonQueue_thenSoftDeletesSevenDayOldCommonQueueTickets() {
		TicketRepository ticketRepository = mock(TicketRepository.class);
		TicketService service = new TicketService(
			ticketRepository,
			mock(TicketRoutingService.class),
			mock(UserRepository.class),
			mock(NotificationService.class)
		);

		service.moveExpiredTicketsToCommonQueue();

		InOrder inOrder = inOrder(ticketRepository);
		inOrder.verify(ticketRepository).moveExpiredTicketsToCommonQueue();
		inOrder.verify(ticketRepository).softDeleteExpiredCommonQueueTickets();
	}
}
