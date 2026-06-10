package com.wip.workipedia.admin.controller;

import com.wip.workipedia.admin.service.AdminManualService;
import com.wip.workipedia.admin.dto.AdminManualCreateRequest;
import com.wip.workipedia.admin.dto.AdminManualUpdateRequest;
import com.wip.workipedia.common.request.BasePageRequest;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.manual.domain.ManualStatus;
import com.wip.workipedia.manual.dto.ManualDetailResponse;
import com.wip.workipedia.manual.dto.ManualSummaryResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/manuals")
@RequiredArgsConstructor
public class AdminManualController {

    private final AdminManualService adminManualService;

    @GetMapping
    public ResponseEntity<PageResponse<ManualSummaryResponse>> list(
            @AuthenticationPrincipal Long actorUserId,
            @RequestParam(required = false) ManualStatus status,
            @Valid BasePageRequest pageRequest) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        return ResponseEntity.ok(adminManualService.findAll(actorUserId, status, pageRequest.toPageable(sort)));
    }

    @PostMapping
    public ResponseEntity<ManualDetailResponse> create(
            @AuthenticationPrincipal Long actorUserId,
            @Valid @RequestBody AdminManualCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminManualService.create(actorUserId, request));
    }

    @PostMapping(value = "/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ManualDetailResponse> createPdf(
            @AuthenticationPrincipal Long actorUserId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam @NotBlank @Size(max = 255) String title,
            @RequestParam(required = false) ManualStatus status,
            @RequestParam(required = false) @Size(max = 50) String version,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminManualService.createFromPdf(actorUserId, departmentId, title, status, version, file));
    }

    @GetMapping("/{manualId}")
    public ResponseEntity<ManualDetailResponse> detail(
            @AuthenticationPrincipal Long actorUserId,
            @PathVariable Long manualId) {
        return ResponseEntity.ok(adminManualService.findById(actorUserId, manualId));
    }

    @PatchMapping("/{manualId}")
    public ResponseEntity<ManualDetailResponse> update(
            @AuthenticationPrincipal Long actorUserId,
            @PathVariable Long manualId,
            @Valid @RequestBody AdminManualUpdateRequest request) {
        return ResponseEntity.ok(adminManualService.update(actorUserId, manualId, request));
    }

    @PatchMapping(value = "/{manualId}/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ManualDetailResponse> updatePdf(
            @AuthenticationPrincipal Long actorUserId,
            @PathVariable Long manualId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @Size(max = 255) String title,
            @RequestParam(required = false) ManualStatus status,
            @RequestParam(required = false) @Size(max = 50) String version,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(
                adminManualService.updateFromPdf(actorUserId, manualId, departmentId, title, status, version, file)
        );
    }

    @DeleteMapping("/{manualId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long actorUserId,
            @PathVariable Long manualId) {
        adminManualService.delete(actorUserId, manualId);
        return ResponseEntity.noContent().build();
    }
}
