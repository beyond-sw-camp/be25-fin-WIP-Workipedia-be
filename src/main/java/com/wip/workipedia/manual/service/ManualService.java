package com.wip.workipedia.manual.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.department.repository.DepartmentRepository;
import com.wip.workipedia.manual.domain.Manual;
import com.wip.workipedia.manual.domain.ManualStatus;
import com.wip.workipedia.manual.dto.ManualCreateRequest;
import com.wip.workipedia.manual.dto.ManualDetailResponse;
import com.wip.workipedia.manual.dto.ManualSummaryResponse;
import com.wip.workipedia.manual.dto.ManualUpdateRequest;
import com.wip.workipedia.manual.repository.ManualRepository;
import com.wip.workipedia.storage.dto.StoredObject;
import com.wip.workipedia.storage.service.StorageService;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.domain.UserRole;
import com.wip.workipedia.user.repository.UserRepository;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManualService {

    // R2 버킷 내 매뉴얼 PDF 가 저장될 폴더(키 접두사)
    private static final String MANUAL_PDF_KEY_PREFIX = "manuals";
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final ManualRepository manualRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PdfTextExtractor pdfTextExtractor;
    private final StorageService storageService;

    // 상태값중 published만 조회.
    public PageResponse<ManualSummaryResponse> findPublished(Pageable pageable) {
        return PageResponse.from(
                manualRepository.findByDeletedAtIsNullAndStatus(ManualStatus.PUBLISHED, pageable)
                        .map(ManualSummaryResponse::from)
        );
    }
    // 
    public ManualDetailResponse findPublishedById(Long manualId) {
        return ManualDetailResponse.from(
                manualRepository.findByManualIdAndDeletedAtIsNullAndStatus(manualId, ManualStatus.PUBLISHED)
                        .orElseThrow(() -> new CustomException(ErrorType.MANUAL_NOT_FOUND))
        );
    }

    // 관리자용 메뉴얼 목록 조회
    public PageResponse<ManualSummaryResponse> findAdminAll(Long actorUserId, ManualStatus status, Pageable pageable) {
        assertSystemAdmin(actorUserId);
        if (status == null) {
            return PageResponse.from(
                    manualRepository.findByDeletedAtIsNull(pageable)
                            .map(ManualSummaryResponse::from)
            );
        }
        return PageResponse.from(
                manualRepository.findByDeletedAtIsNullAndStatus(status, pageable)
                        .map(ManualSummaryResponse::from)
        );
    }

    // 관리자인지 아닌지 확인하고, 메뉴얼 상세 조회.
    public ManualDetailResponse findAdminById(Long actorUserId, Long manualId) {
        assertSystemAdmin(actorUserId);
        return ManualDetailResponse.from(getManual(manualId));
    }

    @Transactional
    public ManualDetailResponse create(Long actorUserId, ManualCreateRequest request) {
        assertSystemAdmin(actorUserId);
        validateDepartment(request.departmentId());

        Manual manual = Manual.create(
                request.departmentId(),
                request.title(),
                request.content(),
                request.status(),
                request.sourceUrl(),
                request.version(),
                actorUserId
        );
        Manual saved = manualRepository.save(manual);
        return ManualDetailResponse.from(saved);
    }

    @Transactional
    public ManualDetailResponse createFromPdf(Long actorUserId, Long departmentId, String title,
            ManualStatus status, String sourceUrl, String version, MultipartFile file) {
        assertSystemAdmin(actorUserId);
        validateDepartment(departmentId);

        String content = pdfTextExtractor.extract(file);
        Manual manual = Manual.create(
                departmentId,
                title,
                content,
                status,
                sourceUrl,
                version,
                actorUserId
        );

        StoredObject stored = uploadPdf(file);
        manual.attachFile(stored.objectKey(), stored.publicUrl());

        Manual saved = manualRepository.save(manual);
        return ManualDetailResponse.from(saved);
    }

    @Transactional
    public ManualDetailResponse update(Long actorUserId, Long manualId, ManualUpdateRequest request) {
        assertSystemAdmin(actorUserId);
        validateDepartment(request.departmentId());

        Manual manual = getManual(manualId);
        manual.update(
                request.departmentId(),
                request.title(),
                request.content(),
                request.status(),
                request.sourceUrl(),
                request.version()
        );
        return ManualDetailResponse.from(manual);
    }

    @Transactional
    public ManualDetailResponse updateFromPdf(Long actorUserId, Long manualId, Long departmentId,
            String title, ManualStatus status, String sourceUrl, String version, MultipartFile file) {
        assertSystemAdmin(actorUserId);
        validateDepartment(departmentId);

        Manual manual = getManual(manualId);
        String content = pdfTextExtractor.extract(file);
        manual.update(
                departmentId,
                title,
                content,
                status,
                sourceUrl,
                version
        );

        String previousFileKey = manual.getFileKey();
        StoredObject stored = uploadPdf(file);
        manual.attachFile(stored.objectKey(), stored.publicUrl());
        deleteStoredFile(previousFileKey);

        return ManualDetailResponse.from(manual);
    }

    @Transactional
    public void delete(Long actorUserId, Long manualId) {
        assertSystemAdmin(actorUserId);
        Manual manual = getManual(manualId);
        manual.delete();
        deleteStoredFile(manual.getFileKey());
    }

    private StoredObject uploadPdf(MultipartFile file) {
        byte[] bytes = readBytes(file);
        return storageService.upload(bytes, MANUAL_PDF_KEY_PREFIX, file.getOriginalFilename(), PDF_CONTENT_TYPE);
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new CustomException(ErrorType.MANUAL_INVALID_FILE, "PDF 파일을 읽는 데 실패했습니다.");
        }
    }

    // 이전 PDF 오브젝트가 있으면 R2에서 제거한다. (없으면 무시)
    private void deleteStoredFile(String fileKey) {
        if (fileKey != null && !fileKey.isBlank()) {
            storageService.deleteObject(fileKey);
        }
    }

    private Manual getManual(Long manualId) {
        return manualRepository.findByManualIdAndDeletedAtIsNull(manualId)
                .orElseThrow(() -> new CustomException(ErrorType.MANUAL_NOT_FOUND));
    }

    // 유저가 어드민인지 확인하는 메서드
    private void assertSystemAdmin(Long actorUserId) {
        User user = userRepository.findById(actorUserId)
                .orElseThrow(() -> new CustomException(ErrorType.MANUAL_FORBIDDEN));
        if (user.getRole() != UserRole.SYSTEM_ADMIN) {
            throw new CustomException(ErrorType.MANUAL_FORBIDDEN);
        }
    }

    private void validateDepartment(Long departmentId) {
        if (departmentId != null && !departmentRepository.existsById(departmentId)) {
            throw new CustomException(ErrorType.NOT_FOUND, "부서를 찾을 수 없습니다. id=" + departmentId);
        }
    }
}
