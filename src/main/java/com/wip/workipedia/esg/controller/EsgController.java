package com.wip.workipedia.esg.controller;

import com.wip.workipedia.esg.dto.EsgResponse;
import com.wip.workipedia.esg.service.EsgService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/esg")
@RequiredArgsConstructor
public class EsgController {

	private final EsgService esgService;

	@GetMapping("/me")
	public ResponseEntity<EsgResponse> getMyEsg(@AuthenticationPrincipal Long userId) {
		return ResponseEntity.ok(esgService.getMyEsg(userId));
	}
}
