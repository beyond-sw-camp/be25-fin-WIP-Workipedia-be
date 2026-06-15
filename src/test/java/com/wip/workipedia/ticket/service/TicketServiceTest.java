package com.wip.workipedia.ticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.notification.service.NotificationService;
import com.wip.workipedia.ticket.domain.RoutingDecision;
import com.wip.workipedia.ticket.domain.Ticket;
import com.wip.workipedia.ticket.domain.TicketRoutingLog;
import com.wip.workipedia.ticket.dto.CreateTicketRequest;
import com.wip.workipedia.ticket.dto.RoutingResult;
import com.wip.workipedia.ticket.repository.TicketRepository;
import com.wip.workipedia.ticket.repository.TicketRoutingLogRepository;
import com.wip.workipedia.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class TicketServiceTest {

	private TicketService buildService(TicketRepository ticketRepository, TicketRoutingLogRepository logRepository) {
		return new TicketService(
			ticketRepository,
			mock(TicketRoutingService.class),
			logRepository,
			mock(UserRepository.class),
			mock(NotificationService.class),
			new ObjectMapper()
		);
	}

	@Test
	void moveExpiredTicketsToCommonQueue_thenSoftDeletesSevenDayOldCommonQueueTickets() {
		TicketRepository ticketRepository = mock(TicketRepository.class);
		TicketService service = buildService(ticketRepository, mock(TicketRoutingLogRepository.class));

		service.moveExpiredTicketsToCommonQueue();

		InOrder inOrder = inOrder(ticketRepository);
		inOrder.verify(ticketRepository).moveExpiredTicketsToCommonQueue();
		inOrder.verify(ticketRepository).softDeleteExpiredCommonQueueTickets();
	}

	@Test
	void saveTicket_시_라우팅_로그가_저장된다() {
		TicketRepository ticketRepository = mock(TicketRepository.class);
		TicketRoutingLogRepository logRepository = mock(TicketRoutingLogRepository.class);
		TicketService service = buildService(ticketRepository, logRepository);

		Ticket savedTicket = mock(Ticket.class);
		when(savedTicket.getTicketId()).thenReturn(1L);
		when(savedTicket.getRequesterId()).thenReturn(1L);
		when(ticketRepository.save(any())).thenReturn(savedTicket);

		RoutingResult result = new RoutingResult(
			null, null, null, null, null,
			RoutingDecision.COMMON_QUEUE,
			List.of("테스트"),
			List.of()
		);

		service.saveTicket(1L, new CreateTicketRequest(null, null, "제목", "내용"), result);

		verify(logRepository).save(any(TicketRoutingLog.class));
	}

	@Test
	void saveTicket_시_라우팅_로그에_decision이_저장된다() {
		TicketRepository ticketRepository = mock(TicketRepository.class);
		TicketRoutingLogRepository logRepository = mock(TicketRoutingLogRepository.class);
		TicketService service = buildService(ticketRepository, logRepository);

		Ticket savedTicket = mock(Ticket.class);
		when(savedTicket.getTicketId()).thenReturn(1L);
		when(savedTicket.getRequesterId()).thenReturn(1L);
		when(ticketRepository.save(any())).thenReturn(savedTicket);

		RoutingResult result = new RoutingResult(
			2L, "개발팀", null, null, "cross-encoder-v1@local",
			RoutingDecision.AUTO_ASSIGNED,
			List.of("R&R 매칭"),
			List.of()
		);

		service.saveTicket(1L, new CreateTicketRequest(null, null, "제목", "내용"), result);

		assertThat(result.decision()).isEqualTo(RoutingDecision.AUTO_ASSIGNED);
		assertThat(result.modelVersion()).isEqualTo("cross-encoder-v1@local");
	}
}
