package com.wip.workipedia.admin.esg.controller;

import com.wip.workipedia.admin.esg.dto.InfraEsgSummaryResponse;
import com.wip.workipedia.admin.esg.service.InfraEsgSummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/esg")
public class InfraEsgAdminController {

    private final InfraEsgSummaryService infraEsgSummaryService;

    public InfraEsgAdminController(InfraEsgSummaryService infraEsgSummaryService) {
        this.infraEsgSummaryService = infraEsgSummaryService;
    }

    @GetMapping("/infra")
    public ResponseEntity<InfraEsgSummaryResponse> getInfraEsgSummary() {
        return ResponseEntity.ok(infraEsgSummaryService.getSummary());
    }
}
