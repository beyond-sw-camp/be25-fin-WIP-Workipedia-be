package com.wip.workipedia.admin.service;

import com.wip.workipedia.admin.domain.ManualVersion;
import com.wip.workipedia.admin.dto.AdminManualCreateRequest;
import com.wip.workipedia.admin.dto.AdminManualUpdateRequest;
import com.wip.workipedia.admin.repository.ManualVersionRepository;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.department.repository.DepartmentRepository;
import com.wip.workipedia.manual.domain.Manual;
import com.wip.workipedia.manual.domain.ManualStatus;
import com.wip.workipedia.manual.dto.ManualDetailResponse;
import com.wip.workipedia.manual.dto.ManualSummaryResponse;
import com.wip.workipedia.manual.repository.ManualRepository;
import com.wip.workipedia.storage.service.StorageService;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.domain.UserRole;
import com.wip.workipedia.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminManualService {

    private final ManualRepository manualRepository;
    private final ManualVersionRepository manualVersionRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PdfTextExtractor pdfTextExtractor;
    private final StorageService storageService;

    public PageResponse<ManualSummaryResponse> findAll(Long actorUserId, ManualStatus status, Pageable pageable) {
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

    public ManualDetailResponse findById(Long actorUserId, Long manualId) {
        assertSystemAdmin(actorUserId);
        return ManualDetailResponse.from(getManual(manualId));
    }

    @Transactional
    public ManualDetailResponse create(Long actorUserId, AdminManualCreateRequest request) {
        assertSystemAdmin(actorUserId);

        String manualNum = normalizeInitialManualNum(request.version());
        Long departmentId = validateDepartmentId(request.departmentId());
        Manual manual = Manual.create(
                departmentId,
                request.title(),
                request.content(),
                request.status(),
                request.sourceUrl(),
                manualNum,
                actorUserId
        );
        Manual savedManual = manualRepository.save(manual);
        saveVersion(savedManual, actorUserId, manualNum, "INITIAL_CREATE");
        return ManualDetailResponse.from(savedManual);
    }

    @Transactional
    public ManualDetailResponse createFromPdf(Long actorUserId, Long departmentId, String title,
            ManualStatus status, String sourceUrl, String version, MultipartFile file) {
        assertSystemAdmin(actorUserId);

        String content = pdfTextExtractor.extract(file);
        String uploadedUrl = storageService.uploadFile("manuals", file);
        String manualNum = normalizeInitialManualNum(version);
        Manual manual = Manual.create(validateDepartmentId(departmentId), title, content, status, uploadedUrl, manualNum, actorUserId);
        Manual savedManual = manualRepository.save(manual);
        saveVersion(savedManual, actorUserId, manualNum, "INITIAL_PDF_UPLOAD");
        return ManualDetailResponse.from(savedManual);
    }

    @Transactional
    public ManualDetailResponse update(Long actorUserId, Long manualId, AdminManualUpdateRequest request) {
        assertSystemAdmin(actorUserId);

        Manual manual = getManual(manualId);
        String manualNum = resolveManualNum(manual.getManualId(), request.version());
        manual.update(
                validateDepartmentId(request.departmentId()),
                request.title(),
                request.content(),
                request.status(),
                request.sourceUrl(),
                manualNum
        );
        saveVersion(manual, actorUserId, manualNum, request.updateReason());
        return ManualDetailResponse.from(manual);
    }

    @Transactional
    public ManualDetailResponse updateFromPdf(Long actorUserId, Long manualId, Long departmentId,
            String title, ManualStatus status, String sourceUrl, String version, MultipartFile file) {
        assertSystemAdmin(actorUserId);

        Manual manual = getManual(manualId);
        String content = pdfTextExtractor.extract(file);
        String uploadedUrl = storageService.uploadFile("manuals", file);
        String manualNum = resolveManualNum(manual.getManualId(), version);
        manual.update(validateDepartmentId(departmentId), title, content, status, uploadedUrl, manualNum);
        saveVersion(manual, actorUserId, manualNum, "PDF_UPLOAD");
        return ManualDetailResponse.from(manual);
    }

    @Transactional
    public void delete(Long actorUserId, Long manualId) {
        assertSystemAdmin(actorUserId);
        getManual(manualId).delete();
    }

    private Manual getManual(Long manualId) {
        return manualRepository.findByManualIdAndDeletedAtIsNull(manualId)
                .orElseThrow(() -> new CustomException(ErrorType.MANUAL_NOT_FOUND));
    }

    private void assertSystemAdmin(Long actorUserId) {
        User user = userRepository.findById(actorUserId)
                .orElseThrow(() -> new CustomException(ErrorType.MANUAL_FORBIDDEN));
        if (user.getRole() != UserRole.SYSTEM_ADMIN) {
            throw new CustomException(ErrorType.MANUAL_FORBIDDEN);
        }
    }

    private Long validateDepartmentId(Long departmentId) {
        if (departmentId == null) {
            return null;
        }
        if (!departmentRepository.findByDepartmentIdAndDeletedAtIsNull(departmentId).isPresent()) {
            throw new CustomException(ErrorType.DEPARTMENT_NOT_FOUND);
        }
        return departmentId;
    }

    private void saveVersion(Manual manual, Long actorUserId, String requestedVersion, String updateReason) {
        String manualNum = resolveManualNum(manual.getManualId(), requestedVersion);
        if (manualVersionRepository.existsByManualManualIdAndManualNumAndDeletedAtIsNull(manual.getManualId(), manualNum)) {
            throw new CustomException(ErrorType.CONFLICT, "Manual version already exists. manualNum=" + manualNum);
        }
        manualVersionRepository.save(ManualVersion.create(manual, actorUserId, manualNum, normalizeUpdateReason(updateReason)));
    }

    private String normalizeInitialManualNum(String requestedVersion) {
        if (requestedVersion != null && !requestedVersion.isBlank()) {
            return requestedVersion;
        }
        return "v1";
    }

    private String resolveManualNum(Long manualId, String requestedVersion) {
        if (requestedVersion != null && !requestedVersion.isBlank()) {
            return requestedVersion;
        }
        return manualVersionRepository.findTopByManualManualIdAndDeletedAtIsNullOrderByManualVersionIdDesc(manualId)
                .map(ManualVersion::getManualNum)
                .map(this::incrementManualNum)
                .orElse("v1");
    }

    private String incrementManualNum(String latestManualNum) {
        if (latestManualNum != null && latestManualNum.matches("v\\d+")) {
            int number = Integer.parseInt(latestManualNum.substring(1));
            return "v" + (number + 1);
        }
        return "v" + System.currentTimeMillis();
    }

    private String normalizeUpdateReason(String updateReason) {
        if (updateReason == null || updateReason.isBlank()) {
            return "ADMIN_UPDATE";
        }
        return updateReason;
    }
}
