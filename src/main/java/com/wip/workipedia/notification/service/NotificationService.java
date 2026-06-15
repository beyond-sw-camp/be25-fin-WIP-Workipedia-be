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

    public void createTicketReassignedNotification(Long userId, Ticket ticket) {
        createAfterCommit("ticket reassigned notification", () ->
                notificationRepository.save(Notification.create(
                        userId,
                        NotificationType.TICKET_REASSIGNED,
                        ticketTitle(NotificationType.TICKET_REASSIGNED),
                        ticket.getTitle(),
                        NotificationTargetType.TICKET,
                        ticket.getTicketId(),
                        "/me/tickets/" + ticket.getTicketId()
                )));
    }

    public void createWorkiQuestionCreated(Long userId, Long questionId, String questionTitle) {
        createAfterCommit("worki question created notification", () ->
                notificationRepository.save(Notification.create(
                        userId,
                        NotificationType.WORKI_QUESTION_CREATED,
                        "워키 질문 등록",
                        questionTitle,
                        NotificationTargetType.WORKI_QUESTION,
                        questionId,
                        "/worki/questions/" + questionId
                )));
    }
    public void createWorkiQuestionAnswered(Long userId, Long questionId, String questionTitle) {
        createAfterCommit("worki question answered notification", () ->
                notificationRepository.save(Notification.create(
                        userId,
                        NotificationType.WORKI_QUESTION_ANSWERED,
                        "워키 답변 완료",
                        questionTitle,
                        NotificationTargetType.WORKI_QUESTION,
                        questionId,
                        "/worki/questions/" + questionId
                )));
    }
    public void createWorkiAnswerAccepted(Long userId, Long answerId, Long questionId, String questionTitle) {
        createAfterCommit("worki answer accepted notification", () ->
                notificationRepository.save(Notification.create(
                        userId,
                        NotificationType.WORKI_ANSWER_ACCEPTED,
                        "워키 답변 채택",
                        questionTitle,
                        NotificationTargetType.WORKI_ANSWER,
                        answerId,
                        "/worki/questions/" + questionId
                )));
    }

    public void createManualUpdated(Long userId, Long manualId, String manualTitle) {
        createManualUpdated(userId, manualId, manualTitle, null, null);
    }

    public void createManualUpdated(Long userId, Long manualId, String manualTitle, String manualVersion) {
        createManualUpdated(userId, manualId, manualTitle, manualVersion, null);
    }

    // 매뉴얼 업데이트 알림은 알림창에서 버전 정보와 수정 요약을 함께 보여준다.
    public void createManualUpdated(
            Long userId,
            Long manualId,
            String manualTitle,
            String manualVersion,
            String updateSummary
    ) {
        String versionText = manualVersion == null || manualVersion.isBlank()
                ? "새 버전"
                : manualVersion;
        String summaryText = updateSummary == null || updateSummary.isBlank()
                ? ""
                : " 수정 내용: " + updateSummary;
        createAfterCommit("manual updated notification", () ->
                notificationRepository.save(Notification.create(
                        userId,
                        NotificationType.MANUAL_UPDATED,
                        "매뉴얼이 업데이트되었습니다",
                        manualTitle + " 매뉴얼이 " + versionText + "으로 업데이트 되었습니다." + summaryText,
                        NotificationTargetType.MANUAL,
                        manualId,
                        "/manuals/" + manualId
                )));
    }

    // 활성화된 관리자 수기 지식은 매뉴얼 탭에서 함께 조회되는 지식성 콘텐츠 알림으로 생성한다.
    public void createDirectDataActivated(Long userId, Long directDataId, String directDataTitle) {
        // 수기 지식은 별도 탭을 만들지 않고 매뉴얼 탭에서 함께 조회되는 지식성 알림으로 저장한다.
        createAfterCommit("direct data activated notification", () ->
                notificationRepository.save(Notification.create(
                        userId,
                        NotificationType.DIRECT_DATA_ACTIVATED,
                        "관리자 수기 지식 등록",
                        directDataTitle,
                        NotificationTargetType.DIRECT_DATA,
                        directDataId,
                        // 프론트는 알림 클릭 시 이 경로로 활성화된 수기 지식 상세 화면을 라우팅한다.
                        "/direct-data/" + directDataId
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
            case TICKET_REASSIGNED -> "티켓 담당 부서 재배정";
            case TICKET_COMPLETED -> "티켓 답변 완료";
            case TICKET_DELETED -> "티켓 삭제";
            default -> "티켓 알림";
        };
    }

    // 알림은 핵심 도메인 트랜잭션이 성공적으로 커밋된 뒤 생성한다.
    // 알림 저장 실패가 답변 등록 및 티켓 상태 변경 같은 본 기능을 롤백시키지 않도록 분리한다.
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

    // 알림 저장은 별도 트랜잭션에서 수행하고, 실패 시 로그만 남긴다.
    // 알림은 부가 기능이므로 저장 실패를 호출 도메인으로 전파하지 않는다.
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
