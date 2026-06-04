package com.wip.workipedia.auth.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.mail", name = "sender", havingValue = "smtp")
@RequiredArgsConstructor
// 운영 환경에서 사용하는 인증코드 발송 구현체입니다.
// APP_MAIL_SENDER=smtp 설정과 spring.mail.* SMTP 설정이 있을 때 실제 이메일을 발송합니다.
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
}
