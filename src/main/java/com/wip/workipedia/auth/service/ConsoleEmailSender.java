package com.wip.workipedia.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

// 로컬 개발 환경에서 사용하는 인증코드 발송 구현체입니다.
// 실제 이메일은 발송하지 않고 서버 콘솔 로그에 인증코드를 출력합니다.
// Postman 테스트 시 콘솔에 출력된 인증코드를 /auth/signup/code/verify 요청에 사용합니다.
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
