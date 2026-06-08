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

	// auth
	AUTH_EMAIL_VERIFICATION_REQUIRED("auth-001", "이메일 인증이 필요합니다.", HttpStatus.BAD_REQUEST),
	AUTH_EMAIL_CODE_MISMATCH("auth-002", "인증코드가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
	AUTH_DUPLICATE_EMAIL("auth-003", "이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),
	AUTH_DUPLICATE_EMPLOYEE_ID("auth-004", "이미 사용 중인 사번입니다.", HttpStatus.CONFLICT),
	AUTH_DEPARTMENT_NOT_FOUND("auth-005", "부서를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	AUTH_EMAIL_SEND_FAILED("auth-006", "인증코드 이메일 발송에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	AUTH_INVALID_CREDENTIALS("auth-007", "사번 또는 비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
	AUTH_INACTIVE_USER("auth-008", "비활성화된 사용자입니다.", HttpStatus.FORBIDDEN),
	AUTH_REFRESH_TOKEN_REQUIRED("auth-009", "Refresh Token이 필요합니다.", HttpStatus.UNAUTHORIZED),
	AUTH_REFRESH_TOKEN_INVALID("auth-010", "유효하지 않은 Refresh Token입니다.", HttpStatus.UNAUTHORIZED),
	AUTH_USER_NOT_FOUND("auth-011", "일치하는 사용자 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	AUTH_PASSWORD_RESET_VERIFICATION_REQUIRED("auth-012", "비밀번호 재설정 인증이 필요합니다.", HttpStatus.BAD_REQUEST),

	// department
	DEPARTMENT_NOT_FOUND("department-001", "부서를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	DEPARTMENT_DUPLICATE_NAME("department-002", "이미 사용 중인 부서명입니다.", HttpStatus.CONFLICT),
	DEPARTMENT_IN_USE("department-003", "사용자가 소속된 부서는 삭제할 수 없습니다.", HttpStatus.CONFLICT),

	// ticket
	TICKET_NOT_FOUND("ticket-001", "티켓을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

	// worki
	WORKI_NOT_FOUND("worki-001", "워키 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	WORKI_FORBIDDEN("worki-002", "해당 작업에 대한 권한이 없습니다.", HttpStatus.FORBIDDEN),
	WORKI_POLICY_VIOLATION("worki-003", "현재 상태에서 허용되지 않는 작업입니다.", HttpStatus.CONFLICT),

	// notification
	NOTIFICATION_NOT_FOUND("notification-001", "알림을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	NOTIFICATION_FORBIDDEN("notification-002", "본인 알림이 아닙니다.", HttpStatus.FORBIDDEN),

	// flash-chat
	FLASH_CHAT_COOLDOWN("flash-chat-001", "메시지 전송 쿨다운 중입니다.", HttpStatus.TOO_MANY_REQUESTS),
	FLASH_CHAT_BANNED_WORD("flash-chat-002", "금지어가 포함된 메시지입니다.", HttpStatus.BAD_REQUEST),
	FLASH_CHAT_MESSAGE_NOT_FOUND("flash-chat-003", "메시지를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	FLASH_CHAT_POLICY_NOT_FOUND("flash-chat-004", "Flash Chat 정책을 찾을 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

	private final String status;
	private final String message;
	private final HttpStatus httpStatus;
}
