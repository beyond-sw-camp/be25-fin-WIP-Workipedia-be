package com.wip.workipedia.worki.exception;

import com.wip.workipedia.worki.controller.WorkiAnswerController;
import com.wip.workipedia.worki.controller.WorkiQuestionController;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 워키 컨트롤러에만 한정한 advice. 전역 예외 처리기(Step1)와 충돌하지 않도록 assignableTypes로 범위를 제한한다.
// HIGHEST_PRECEDENCE: GlobalExceptionHandler의 catch-all(Exception.class)보다 먼저 매칭되어 404/403/409가 500으로 묻히지 않도록 한다.
@Order(Ordered.HIGHEST_PRECEDENCE)
// 이걸 통해서 이 에러핸들러는 여기 컨트롤러에서만 작동함.
@RestControllerAdvice(assignableTypes = {WorkiQuestionController.class, WorkiAnswerController.class})
public class WorkiExceptionHandler {

    @ExceptionHandler(WorkiNotFoundException.class)
    public ResponseEntity<WorkiErrorResponse> handleNotFound(WorkiNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new WorkiErrorResponse("WORKI_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(WorkiAccessDeniedException.class)
    public ResponseEntity<WorkiErrorResponse> handleForbidden(WorkiAccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new WorkiErrorResponse("WORKI_FORBIDDEN", e.getMessage()));
    }

    @ExceptionHandler(WorkiPolicyViolationException.class)
    public ResponseEntity<WorkiErrorResponse> handlePolicy(WorkiPolicyViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new WorkiErrorResponse("WORKI_POLICY_VIOLATION", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<WorkiErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("요청 값이 올바르지 않습니다.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new WorkiErrorResponse("VALIDATION_ERROR", message));
    }
}
