package com.wip.workipedia.manual.controller;

import com.wip.workipedia.common.request.BasePageRequest;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.manual.domain.ManualStatus;
import com.wip.workipedia.manual.dto.ManualCreateRequest;
import com.wip.workipedia.manual.dto.ManualDetailResponse;
import com.wip.workipedia.manual.dto.ManualSummaryResponse;
import com.wip.workipedia.manual.dto.ManualUpdateRequest;
import com.wip.workipedia.manual.service.ManualService;
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

// 여기의 경우는 관리자가 사용하는 하나의 컨트롤러 관리자 확인은 ManualService에서 각각의 메서드에서 assertSystemAdmin 메서드로 확인함
@RestController
@RequestMapping("/api/v1/admin/manuals")
@RequiredArgsConstructor
public class AdminManualController {

    private final ManualService manualService;

    @GetMapping
    public ResponseEntity<PageResponse<ManualSummaryResponse>> list(
            @AuthenticationPrincipal Long actorUserId,
            @RequestParam(required = false) ManualStatus status,
            @Valid BasePageRequest pageRequest) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        return ResponseEntity.ok(manualService.findAdminAll(actorUserId, status, pageRequest.toPageable(sort)));
    }
    // 메뉴얼 등록
    @PostMapping
    public ResponseEntity<ManualDetailResponse> create(
            @AuthenticationPrincipal Long actorUserId,
            @Valid @RequestBody ManualCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(manualService.create(actorUserId, request));
    }

    // PDF 파일에서 본문을 추출해 메뉴얼로 등록한다. 원본 PDF 저장/다운로드는 별도 첨부 도메인 연동 전까지 하지 않는다.
    @PostMapping(value = "/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ManualDetailResponse> createPdf(
            @AuthenticationPrincipal Long actorUserId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam @NotBlank @Size(max = 255) String title,
            @RequestParam(required = false) ManualStatus status,
            @RequestParam(required = false) @Size(max = 500) String sourceUrl,
            @RequestParam(required = false) @Size(max = 50) String version,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(manualService.createFromPdf(actorUserId, departmentId, title, status, sourceUrl, version, file));
    }


   // 관리자용 메뉴얼 디테일하게 보기. 사용자는 published만 볼수 있고, 관리자는 모든 상태의 메뉴얼을 볼수가 있음
    @GetMapping("/{manualId}")
    public ResponseEntity<ManualDetailResponse> detail(
            @AuthenticationPrincipal Long actorUserId,
            @PathVariable Long manualId) {
        return ResponseEntity.ok(manualService.findAdminById(actorUserId, manualId));
    }

    // 메뉴얼 수정  
    @PatchMapping("/{manualId}")
    public ResponseEntity<ManualDetailResponse> update(
            @AuthenticationPrincipal Long actorUserId,
            @PathVariable Long manualId,
            @Valid @RequestBody ManualUpdateRequest request) {
        return ResponseEntity.ok(manualService.update(actorUserId, manualId, request));
    }

    // PDF 파일을 다시 추출해 기존 메뉴얼 본문을 교체한다.
    @PatchMapping(value = "/{manualId}/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ManualDetailResponse> updatePdf(
            @AuthenticationPrincipal Long actorUserId,
            @PathVariable Long manualId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @Size(max = 255) String title,
            @RequestParam(required = false) ManualStatus status,
            @RequestParam(required = false) @Size(max = 500) String sourceUrl,
            @RequestParam(required = false) @Size(max = 50) String version,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(
                manualService.updateFromPdf(actorUserId, manualId, departmentId, title, status, sourceUrl, version, file)
        );
    }
    // 메뉴얼 삭제. 
    @DeleteMapping("/{manualId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long actorUserId,
            @PathVariable Long manualId) {
        manualService.delete(actorUserId, manualId);
        return ResponseEntity.noContent().build();
    }
}
