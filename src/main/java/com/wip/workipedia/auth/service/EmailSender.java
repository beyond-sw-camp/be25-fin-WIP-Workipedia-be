package com.wip.workipedia.auth.service;

public interface EmailSender {

	void sendSignupCode(String email, String code);
}
