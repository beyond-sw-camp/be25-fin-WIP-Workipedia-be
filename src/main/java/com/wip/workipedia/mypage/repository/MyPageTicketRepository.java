package com.wip.workipedia.mypage.repository;

import java.util.List;
import java.util.Optional;

import com.wip.workipedia.ticket.domain.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface MyPageTicketRepository extends Repository<Ticket, Long> {

    @Query(
            value = """
            SELECT
                t.ticket_id AS ticketId,
                t.title AS title,
                t.assigned_department_id AS assignedDepartmentId,
                d.department_name AS assignedDepartmentName,
                t.status AS status,
                t.created_at AS createdAt
            FROM tickets t
            LEFT JOIN departments d
                ON t.assigned_department_id = d.department_id
            WHERE t.requester_id = :requesterId
                AND t.status IN (:statuses)
                AND t.deleted_at IS NULL
            ORDER BY t.created_at DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM tickets t
            WHERE t.requester_id = :requesterId
                AND t.status IN (:statuses)
                AND t.deleted_at IS NULL
            """,
            nativeQuery = true
    )
    Page<MyPageTicketProjection> findMyTickets(
            Long requesterId,
            List<String> statuses,
            Pageable pageable
    );

    @Query(
            value = """
            SELECT
                t.ticket_id AS ticketId,
                t.title AS title,
                t.content AS content,
                t.assigned_department_id AS assignedDepartmentId,
                d.department_name AS assignedDepartmentName,
                t.status AS status,
                t.created_at AS createdAt
            FROM tickets t
            LEFT JOIN departments d
                ON t.assigned_department_id = d.department_id
            WHERE t.ticket_id = :ticketId
                AND t.requester_id = :requesterId
                AND t.deleted_at IS NULL
            """,
            nativeQuery = true
    )
    Optional<MyTicketDetailProjection> findMyTicketDetail(Long requesterId, Long ticketId);
}
