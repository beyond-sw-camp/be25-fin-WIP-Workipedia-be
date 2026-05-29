package com.wip.workipedia.worki.exception;

// 상태 위반(비WAITING 수정, 채택된 질문에 답변, 재채택 등) 정책 위반.
public class WorkiPolicyViolationException extends RuntimeException {
    public WorkiPolicyViolationException(String message) {
        super(message);
    }
}
