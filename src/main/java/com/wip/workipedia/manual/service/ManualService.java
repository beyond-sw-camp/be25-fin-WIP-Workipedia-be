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
public class ManualService {

    private final ManualRepository manualRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PdfTextExtractor pdfTextExtractor;

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
        return ManualDetailResponse.from(manualRepository.save(manual));
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
        return ManualDetailResponse.from(manualRepository.save(manual));
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
        return ManualDetailResponse.from(manual);
    }

    @Transactional
    public void delete(Long actorUserId, Long manualId) {
        assertSystemAdmin(actorUserId);
        Manual manual = getManual(manualId);
        manual.delete();
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

    private void validateDepartment(Long departmentId) {
        if (departmentId != null && !departmentRepository.existsById(departmentId)) {
            throw new CustomException(ErrorType.NOT_FOUND, "부서를 찾을 수 없습니다. id=" + departmentId);
        }
    }
}
