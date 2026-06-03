package com.wip.workipedia.auth.service;

// 회원가입 인증코드 발송 방식을 분리하기 위한 인터페이스입니다.
// 로컬에서는 ConsoleEmailSender, 운영에서는 SmtpEmailSender 구현체를 사용합니다.
public interface EmailSender {

	void sendSignupCode(String email, String code);
}
