package com.wip.workipedia.worki.exception;

// 전역 ApiResponse 엔벨로프(Step1 통합) 적용 전까지 사용하는 임시 에러 본문.
public record WorkiErrorResponse(String code, String message) {
}
