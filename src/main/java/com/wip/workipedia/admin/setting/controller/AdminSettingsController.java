package com.wip.workipedia.admin.setting.controller;

import com.wip.workipedia.admin.setting.dto.AdminSettingsSummaryResponse;
import com.wip.workipedia.admin.setting.service.AdminSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/settings")
@RequiredArgsConstructor
public class AdminSettingsController {

	private final AdminSettingsService adminSettingsService;

	@GetMapping("/summary")
	public ResponseEntity<AdminSettingsSummaryResponse> summary() {
		return ResponseEntity.ok(adminSettingsService.getSummary());
	}
}
