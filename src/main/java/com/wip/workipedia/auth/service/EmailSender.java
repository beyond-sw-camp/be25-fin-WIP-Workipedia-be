package com.wip.workipedia.auth.service;

// 이메일 인증코드 발송 규칙을 정의하고 방식을 분리하기 위한 인터페이스입니다.
public interface EmailSender {

	void sendSignupCode(String email, String code);

	void sendPasswordResetCode(String email, String code);
}
