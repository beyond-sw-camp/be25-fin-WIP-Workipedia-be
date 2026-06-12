package com.wip.workipedia.admin.directdata.controller;

import com.wip.workipedia.admin.directdata.dto.AdminDirectDataRequest;
import com.wip.workipedia.admin.directdata.dto.AdminDirectDataResponse;
import com.wip.workipedia.admin.directdata.service.AdminDirectDataService;
import com.wip.workipedia.common.request.BasePageRequest;
import com.wip.workipedia.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/direct-data")
@RequiredArgsConstructor
public class AdminDirectDataController {

    private final AdminDirectDataService adminDirectDataService;

    @GetMapping
    public ResponseEntity<PageResponse<AdminDirectDataResponse>> findAll(
            @AuthenticationPrincipal Long actorUserId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") Boolean includeDeleted,
            @Valid BasePageRequest pageRequest) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        return ResponseEntity.ok(adminDirectDataService.findAll(
                actorUserId,
                isActive,
                category,
                keyword,
                includeDeleted,
                pageRequest.toPageable(sort)
        ));
    }

    @GetMapping("/{directDataId}")
    public ResponseEntity<AdminDirectDataResponse> findById(
            @AuthenticationPrincipal Long actorUserId,
            @PathVariable Long directDataId) {
        return ResponseEntity.ok(adminDirectDataService.findById(actorUserId, directDataId));
    }

    @PostMapping
    public ResponseEntity<AdminDirectDataResponse> create(
            @AuthenticationPrincipal Long actorUserId,
            @Valid @RequestBody AdminDirectDataRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminDirectDataService.create(actorUserId, request));
    }

    @PutMapping("/{directDataId}")
    public ResponseEntity<AdminDirectDataResponse> update(
            @AuthenticationPrincipal Long actorUserId,
            @PathVariable Long directDataId,
            @Valid @RequestBody AdminDirectDataRequest request) {
        return ResponseEntity.ok(adminDirectDataService.update(actorUserId, directDataId, request));
    }

    @DeleteMapping("/{directDataId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long actorUserId,
            @PathVariable Long directDataId) {
        adminDirectDataService.delete(actorUserId, directDataId);
        return ResponseEntity.noContent().build();
    }
}
