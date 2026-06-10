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
                t.assigned_at AS assignedAt,
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
                t.assigned_at AS assignedAt,
                t.created_at AS createdAt,
                t.completed_at AS completedAt,
                ta.ticket_answer_id AS answerId,
                ta.content AS answerContent,
                ta.author_id AS answerAuthorId,
                au.nickname AS answerAuthorNickname,
                au.department_id AS answerAuthorDepartmentId,
                ad.department_name AS answerAuthorDepartmentName,
                ta.created_at AS answeredAt
            FROM tickets t
            LEFT JOIN departments d
                ON t.assigned_department_id = d.department_id
            LEFT JOIN ticket_answers ta
                ON ta.ticket_id = t.ticket_id
                AND ta.deleted_at IS NULL
                AND ta.ticket_answer_id = (
                    SELECT latest_ta.ticket_answer_id
                    FROM ticket_answers latest_ta
                    WHERE latest_ta.ticket_id = t.ticket_id
                        AND latest_ta.deleted_at IS NULL
                    ORDER BY latest_ta.created_at DESC
                    LIMIT 1
                )
            LEFT JOIN users au
                ON ta.author_id = au.user_id
            LEFT JOIN departments ad
                ON au.department_id = ad.department_id
            WHERE t.ticket_id = :ticketId
                AND t.requester_id = :requesterId
                AND t.status <> 'DELETED'
                AND t.deleted_at IS NULL
            """,
            nativeQuery = true
    )
    Optional<MyTicketDetailProjection> findMyTicketDetail(Long requesterId, Long ticketId);
}
