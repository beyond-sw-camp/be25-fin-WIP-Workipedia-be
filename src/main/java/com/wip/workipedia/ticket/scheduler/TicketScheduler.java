package com.wip.workipedia.ticket.scheduler;

import com.wip.workipedia.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketScheduler {

    private final TicketService ticketService;

    @Scheduled(cron = "0 0 * * * *")
    public void moveExpiredTickets() {
        ticketService.moveExpiredTicketsToCommonQueue();
    }
}
