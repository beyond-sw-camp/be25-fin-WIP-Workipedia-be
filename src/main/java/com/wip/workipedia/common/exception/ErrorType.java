package com.wip.workipedia.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorType {

	BAD_REQUEST("bad_request", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
	UNAUTHORIZED("unauthorized", "인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED),
	FORBIDDEN("forbidden", "권한이 없습니다.", HttpStatus.FORBIDDEN),
	NOT_FOUND("not_found", "리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	CONFLICT("conflict", "요청이 현재 상태와 충돌합니다.", HttpStatus.CONFLICT),
	INTERNAL_ERROR("internal_error", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

	// ticket
	TICKET_NOT_FOUND("ticket-001", "티켓을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

	// worki
	WORKI_NOT_FOUND("worki-001", "워키 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	WORKI_FORBIDDEN("worki-002", "해당 작업에 대한 권한이 없습니다.", HttpStatus.FORBIDDEN),
	WORKI_POLICY_VIOLATION("worki-003", "현재 상태에서 허용되지 않는 작업입니다.", HttpStatus.CONFLICT),

	// notification
	NOTIFICATION_NOT_FOUND("notification-001", "알림을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	NOTIFICATION_FORBIDDEN("notification-002", "본인 알림이 아닙니다.", HttpStatus.FORBIDDEN);

	private final String status;
	private final String message;
	private final HttpStatus httpStatus;
}
