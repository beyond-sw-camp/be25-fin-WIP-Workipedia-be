package com.wip.workipedia.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
	prefix = "app.mail",
	name = "sender",
	havingValue = "console",
	matchIfMissing = true
)
public class ConsoleEmailSender implements EmailSender {

	private static final Logger log = LoggerFactory.getLogger(ConsoleEmailSender.class);

	@Override
	public void sendSignupCode(String email, String code) {
		log.info("[회원가입 인증코드] email={}, code={}", email, code);
	}
}
