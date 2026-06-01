package com.wip.workipedia.auth.service;

import com.wip.workipedia.auth.dto.SignupRequest;
import com.wip.workipedia.auth.dto.SignupResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	@Transactional
	public SignupResponse signup(SignupRequest signupRequest) {
		throw new UnsupportedOperationException("회원가입 도메인 구현이 필요합니다.");
	}
}
