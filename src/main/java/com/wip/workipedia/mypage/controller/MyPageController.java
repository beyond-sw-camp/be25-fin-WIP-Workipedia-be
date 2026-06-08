package com.wip.workipedia.mypage.controller;

import com.wip.workipedia.mypage.dto.MyPageResponse;
import com.wip.workipedia.mypage.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class MyPageController {

	private final MyPageService myPageService;

	// 마이페이지 조회
	@GetMapping("/profile")
	public ResponseEntity<MyPageResponse> getMyPage(
		@AuthenticationPrincipal Long userId
	) {
		MyPageResponse myPageResponse = myPageService.getMyPage(userId);

		return ResponseEntity.ok(myPageResponse);
	}
}
