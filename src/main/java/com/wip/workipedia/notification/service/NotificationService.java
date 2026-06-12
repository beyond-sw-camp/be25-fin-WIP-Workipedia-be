package com.wip.workipedia.notification.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.notification.domain.Notification;
import com.wip.workipedia.notification.domain.NotificationTargetType;
import com.wip.workipedia.notification.domain.NotificationTab;
import com.wip.workipedia.notification.domain.NotificationType;
import com.wip.workipedia.notification.dto.NotificationResponse;
import com.wip.workipedia.notification.dto.UnreadCountResponse;
import com.wip.workipedia.notification.repository.NotificationRepository;
import com.wip.workipedia.ticket.domain.Ticket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final PlatformTransactionManager transactionManager;

    public PageResponse<NotificationResponse> list(Long userId, NotificationTab tab, Pageable pageable) {
        // 탭 조회는 현재 도메인 상태가 아니라, 생성된 알림 이력을 기준으로 분류한다.
        if (tab == NotificationTab.TICKET) {
            return PageResponse.from(
                    notificationRepository
                            .findTicketTabNotifications(userId, pageable)
                            .map(NotificationResponse::from));
        }

        if (tab == NotificationTab.WORKI) {
            return PageResponse.from(
                    notificationRepository
                            .findWorkiTabNotifications(userId, pageable)
                            .map(NotificationResponse::from));
        }

        if (tab == NotificationTab.MANUAL) {
            return PageResponse.from(
                    notificationRepository
                            .findManualTabNotifications(userId, pageable)
                            .map(NotificationResponse::from));
        }

        return PageResponse.from(
                notificationRepository
                        .findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId, pageable)
                        .map(NotificationResponse::from));
    }

    public UnreadCountResponse unreadCount(Long userId) {
        return new UnreadCountResponse(
                notificationRepository.countByUserIdAndReadAtIsNullAndDeletedAtIsNull(userId));
    }

    public void createTicketNotification(Long userId, Ticket ticket) {
        // 티켓 알림 타입은 알림 생성 시점의 티켓 상태와 1:1로 매핑한다.
        NotificationType type = switch (ticket.getStatus()) {
            case ASSIGNED -> NotificationType.TICKET_ASSIGNED;
            case COMPLETED -> NotificationType.TICKET_COMPLETED;
            case DELETED -> NotificationType.TICKET_DELETED;
            default -> null;
        };
        if (type == null) {
            return;
        }

        createAfterCommit("ticket notification", () ->
                notificationRepository.save(Notification.create(
                        userId,
                        type,
                        ticketTitle(type),
                        ticket.getTitle(),
                        NotificationTargetType.TICKET,
                        ticket.getTicketId(),
                        "/me/tickets/" + ticket.getTicketId()
                )));
    }

    public void createWorkiQuestionCreated(Long userId, Long questionId, String questionTitle) {
        createWorkiQuestionCreated(userId, questionId, questionTitle, null);
    }

    public void createWorkiQuestionCreated(Long userId, Long questionId, String questionTitle, Integer pointAmount) {
        createAfterCommit("worki question created notification", () ->
                notificationRepository.save(Notification.create(
                        userId,
                        NotificationType.WORKI_QUESTION_CREATED,
                        "워키 질문 등록",
                        questionTitle,
                        NotificationTargetType.WORKI_QUESTION,
                        questionId,
                        "/worki/questions/" + questionId,
                        pointAmount
                )));
    }

    public void createWorkiQuestionAnswered(Long userId, Long questionId, String questionTitle) {
        createWorkiQuestionAnswered(userId, questionId, questionTitle, null);
    }

    public void createWorkiQuestionAnswered(Long userId, Long questionId, String questionTitle, Integer pointAmount) {
        createAfterCommit("worki question answered notification", () ->
                notificationRepository.save(Notification.create(
                        userId,
                        NotificationType.WORKI_QUESTION_ANSWERED,
                        "워키 답변 완료",
                        questionTitle,
                        NotificationTargetType.WORKI_QUESTION,
                        questionId,
                        "/worki/questions/" + questionId,
                        pointAmount
                )));
    }

    public void createWorkiAnswerAccepted(Long userId, Long answerId, Long questionId, String questionTitle) {
        createWorkiAnswerAccepted(userId, answerId, questionId, questionTitle, null);
    }

    public void createWorkiAnswerAccepted(
            Long userId,
            Long answerId,
            Long questionId,
            String questionTitle,
            Integer pointAmount
    ) {
        createAfterCommit("worki answer accepted notification", () ->
                notificationRepository.save(Notification.create(
                        userId,
                        NotificationType.WORKI_ANSWER_ACCEPTED,
                        "워키 답변 채택",
                        questionTitle,
                        NotificationTargetType.WORKI_ANSWER,
                        answerId,
                        "/worki/questions/" + questionId,
                        pointAmount
                )));
    }

    public void createManualUpdated(Long userId, Long manualId, String manualTitle) {
        createManualUpdated(userId, manualId, manualTitle, null);
    }

    public void createManualUpdated(Long userId, Long manualId, String manualTitle, String manualVersion) {
        String versionText = manualVersion == null || manualVersion.isBlank()
                ? "새 버전"
                : manualVersion;
        createAfterCommit("manual updated notification", () ->
                notificationRepository.save(Notification.create(
                        userId,
                        NotificationType.MANUAL_UPDATED,
                        "매뉴얼이 업데이트되었습니다",
                        manualTitle + " 매뉴얼이 " + versionText + "으로 업데이트 되었습니다.",
                        NotificationTargetType.MANUAL,
                        manualId,
                        "/manuals/" + manualId
                )));
    }

    @Transactional
    public void markAsRead(Long actorUserId, Long notificationId) {
        Notification notification = getOwnedNotification(actorUserId, notificationId);
        notification.markAsRead();
    }

    @Transactional
    public void markAllAsRead(Long actorUserId) {
        notificationRepository.markAllRead(actorUserId);
    }

    @Transactional
    public void delete(Long actorUserId, Long notificationId) {
        Notification notification = getOwnedNotification(actorUserId, notificationId);
        notification.markDeleted();
    }

    private Notification getOwnedNotification(Long actorUserId, Long notificationId) {
        Notification notification = notificationRepository
                .findByNotificationIdAndDeletedAtIsNull(notificationId)
                .orElseThrow(() -> new CustomException(
                        ErrorType.NOTIFICATION_NOT_FOUND,
                        "알림을 찾을 수 없습니다. id=" + notificationId));
        if (!notification.isOwnedBy(actorUserId)) {
            throw new CustomException(ErrorType.NOTIFICATION_FORBIDDEN);
        }
        return notification;
    }

    private String ticketTitle(NotificationType type) {
        return switch (type) {
            case TICKET_ASSIGNED -> "티켓 부서 배정";
            case TICKET_COMPLETED -> "티켓 답변 완료";
            case TICKET_DELETED -> "티켓 삭제";
            default -> "티켓 알림";
        };
    }

    private void createAfterCommit(String context, Runnable notificationCreation) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            createInNewTransaction(context, notificationCreation);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                createInNewTransaction(context, notificationCreation);
            }
        });
    }

    private void createInNewTransaction(String context, Runnable notificationCreation) {
        try {
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            transactionTemplate.executeWithoutResult(status -> notificationCreation.run());
        } catch (RuntimeException exception) {
            log.warn("Failed to create {}.", context, exception);
        }
    }
}
