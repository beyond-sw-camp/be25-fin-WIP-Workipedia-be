package com.wip.workipedia.auth.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

// SMTP 설정을 사용해 인증코드를 실제 이메일로 발송합니다.
@Component
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender {

	private final JavaMailSender javaMailSender;

	@Override
	public void sendSignupCode(String email, String code) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email);
		message.setSubject("[Workipedia] 회원가입 인증코드");
		message.setText("회원가입 인증코드는 " + code + " 입니다.");

		try {
			javaMailSender.send(message);
		} catch (MailException exception) {
			throw new CustomException(ErrorType.AUTH_EMAIL_SEND_FAILED);
		}
	}

	@Override
	public void sendPasswordResetCode(String email, String code) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email);
		message.setSubject("[Workipedia] 비밀번호 재설정 인증코드");
		message.setText("비밀번호 재설정 인증코드는 " + code + " 입니다.");

		try {
			javaMailSender.send(message);
		} catch (MailException exception) {
			throw new CustomException(ErrorType.AUTH_EMAIL_SEND_FAILED);
		}
	}
}
