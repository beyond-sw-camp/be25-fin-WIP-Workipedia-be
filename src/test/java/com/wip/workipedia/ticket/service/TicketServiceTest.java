package com.wip.workipedia.ticket.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.inOrder;

import com.wip.workipedia.notification.service.NotificationService;
import com.wip.workipedia.ticket.repository.TicketRepository;
import com.wip.workipedia.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.mock;

class TicketServiceTest {

	@Test
	void moveExpiredTicketsToCommonQueue_thenSoftDeletesExpiredCommonQueueTicketsAndCreatesNotifications() {
		TicketRepository ticketRepository = mock(TicketRepository.class);
		NotificationService notificationService = mock(NotificationService.class);
		TicketService service = new TicketService(
			ticketRepository,
			mock(TicketRoutingService.class),
			mock(UserRepository.class),
			notificationService
		);
		TicketRepository.ExpiredCommonQueueTicketProjection expiredTicket = expiredTicket(
			100L,
			1L,
			"expired ticket"
		);
		when(ticketRepository.findExpiredCommonQueueTickets()).thenReturn(List.of(expiredTicket));

		service.moveExpiredTicketsToCommonQueue();

		InOrder inOrder = inOrder(ticketRepository);
		inOrder.verify(ticketRepository).moveExpiredTicketsToCommonQueue();
		inOrder.verify(ticketRepository).findExpiredCommonQueueTickets();
		inOrder.verify(ticketRepository).softDeleteExpiredCommonQueueTickets(List.of(100L));
		verify(notificationService).createTicketDeletedNotification(1L, 100L, "expired ticket");
	}

	@Test
	void moveExpiredTicketsToCommonQueue_skipsSoftDeleteAndNotificationWhenNoExpiredCommonQueueTickets() {
		TicketRepository ticketRepository = mock(TicketRepository.class);
		NotificationService notificationService = mock(NotificationService.class);
		TicketService service = new TicketService(
			ticketRepository,
			mock(TicketRoutingService.class),
			mock(UserRepository.class),
			notificationService
		);
		when(ticketRepository.findExpiredCommonQueueTickets()).thenReturn(List.of());

		service.moveExpiredTicketsToCommonQueue();

		verify(ticketRepository).moveExpiredTicketsToCommonQueue();
		verify(ticketRepository).findExpiredCommonQueueTickets();
		verifyNoMoreInteractions(ticketRepository, notificationService);
	}

	private TicketRepository.ExpiredCommonQueueTicketProjection expiredTicket(
		Long ticketId,
		Long requesterId,
		String title
	) {
		return new TicketRepository.ExpiredCommonQueueTicketProjection() {
			@Override
			public Long getTicketId() {
				return ticketId;
			}

			@Override
			public Long getRequesterId() {
				return requesterId;
			}

			@Override
			public String getTitle() {
				return title;
			}
		};
	}
}
