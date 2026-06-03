package com.wip.workipedia.auth.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.mail", name = "sender", havingValue = "smtp")
public class SmtpEmailSender implements EmailSender {

	private final JavaMailSender javaMailSender;

	public SmtpEmailSender(JavaMailSender javaMailSender) {
		this.javaMailSender = javaMailSender;
	}

	@Override
	public void sendSignupCode(String email, String code) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email);
		message.setSubject("[Workipedia] 회원가입 인증코드");
		message.setText("회원가입 인증코드는 " + code + " 입니다.");
		javaMailSender.send(message);
	}
}
