package com.wip.workipedia.admin.manual.service;

import com.wip.workipedia.admin.manual.dto.AdminManualCreateRequest;
import com.wip.workipedia.admin.manual.dto.AdminManualUpdateRequest;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.department.repository.DepartmentRepository;
import com.wip.workipedia.manual.domain.Manual;
import com.wip.workipedia.manual.domain.ManualFile;
import com.wip.workipedia.manual.domain.ManualVersion;
import com.wip.workipedia.manual.domain.ManualStatus;
import com.wip.workipedia.manual.dto.ManualDetailResponse;
import com.wip.workipedia.manual.dto.ManualSummaryResponse;
import com.wip.workipedia.manual.repository.ManualFileRepository;
import com.wip.workipedia.manual.repository.ManualRepository;
import com.wip.workipedia.manual.repository.ManualVersionRepository;
import com.wip.workipedia.storage.dto.StoredObject;
import com.wip.workipedia.storage.service.StorageService;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.domain.UserRole;
import com.wip.workipedia.user.repository.UserRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminManualService {

    private static final String MANUAL_PDF_KEY_PREFIX = "manuals";
    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final String INITIAL_VERSION = "1.0";

    private final ManualRepository manualRepository;
    private final ManualFileRepository manualFileRepository;
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
        Manual manual = getManual(manualId);
        return ManualDetailResponse.from(manual, findFileUrls(manual.getManualId()));
    }

    @Transactional
    public ManualDetailResponse create(Long actorUserId, AdminManualCreateRequest request) {
        assertSystemAdmin(actorUserId);
        validateDuplicateTitle(request.title());

        String manualNum = INITIAL_VERSION;
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
            ManualStatus status, String sourceUrl, List<MultipartFile> files) {
        assertSystemAdmin(actorUserId);
        validateDuplicateTitle(title);

        List<PdfUpload> uploads = readPdfUploads(files);
        String content = extractContent(uploads);
        String manualNum = INITIAL_VERSION;
        Manual manual = Manual.create(validateDepartmentId(departmentId), title, content, status, sourceUrl, manualNum, actorUserId);
        Manual savedManual = manualRepository.save(manual);
        List<StoredObject> storedObjects = uploadPdfFiles(uploads);
        attachFiles(savedManual, storedObjects);
        saveVersion(savedManual, actorUserId, manualNum, "INITIAL_PDF_UPLOAD");
        return ManualDetailResponse.from(savedManual, toFileUrls(storedObjects));
    }

    @Transactional
    public ManualDetailResponse update(Long actorUserId, Long manualId, AdminManualUpdateRequest request) {
        assertSystemAdmin(actorUserId);

        Manual manual = getManual(manualId);
        validateDuplicateTitleForUpdate(request.title(), manual.getManualId());
        String manualNum = resolveNextVersion(manual, fileCount(manual));
        manual.update(
                validateDepartmentId(request.departmentId()),
                request.title(),
                request.content(),
                request.status(),
                request.sourceUrl(),
                manualNum
        );
        saveVersion(manual, actorUserId, manualNum, request.updateReason());
        return ManualDetailResponse.from(manual, findFileUrls(manual.getManualId()));
    }

    @Transactional
    public ManualDetailResponse updateFromPdf(Long actorUserId, Long manualId, Long departmentId,
            String title, ManualStatus status, String sourceUrl, List<MultipartFile> files) {
        assertSystemAdmin(actorUserId);

        Manual manual = getManual(manualId);
        validateDuplicateTitleForUpdate(title, manual.getManualId());
        List<PdfUpload> uploads = readPdfUploads(files);
        String content = extractContent(uploads);
        String manualNum = resolveNextVersion(manual, uploads.size());
        manual.update(validateDepartmentId(departmentId), title, content, status, sourceUrl, manualNum);
        List<ManualFile> previousFiles = findActiveFiles(manual.getManualId());
        List<StoredObject> storedObjects = uploadPdfFiles(uploads);
        replaceFiles(manual, previousFiles, storedObjects);
        saveVersion(manual, actorUserId, manualNum, "PDF_UPLOAD");
        previousFiles.forEach(previousFile -> deleteStoredFileAfterCommit(previousFile.getFileKey()));
        return ManualDetailResponse.from(manual, toFileUrls(storedObjects));
    }

    @Transactional
    public void delete(Long actorUserId, Long manualId) {
        assertSystemAdmin(actorUserId);
        Manual manual = getManual(manualId);
        List<ManualFile> files = findActiveFiles(manual.getManualId());
        manual.delete();
        files.forEach(ManualFile::delete);
        files.forEach(file -> deleteStoredFileAfterCommit(file.getFileKey()));
    }

    private Manual getManual(Long manualId) {
        return manualRepository.findByManualIdAndDeletedAtIsNull(manualId)
                .orElseThrow(() -> new CustomException(ErrorType.MANUAL_NOT_FOUND));
    }

    private void validateDuplicateTitle(String title) {
        if (manualRepository.existsByTitleAndDeletedAtIsNull(title)) {
            throw new CustomException(ErrorType.CONFLICT, "이미 같은 제목의 매뉴얼이 있습니다. 기존 매뉴얼을 수정해주세요.");
        }
    }

    private void validateDuplicateTitleForUpdate(String title, Long manualId) {
        if (title == null || title.isBlank()) {
            return;
        }
        if (manualRepository.existsByTitleAndManualIdNotAndDeletedAtIsNull(title, manualId)) {
            throw new CustomException(ErrorType.CONFLICT, "Manual title already exists.");
        }
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
        if (!departmentRepository.existsByDepartmentIdAndDeletedAtIsNull(departmentId)) {
            throw new CustomException(ErrorType.DEPARTMENT_NOT_FOUND);
        }
        return departmentId;
    }

    private List<StoredObject> uploadPdfFiles(List<PdfUpload> uploads) {
        List<StoredObject> storedObjects = new ArrayList<>();
        try {
            for (PdfUpload upload : uploads) {
                StoredObject stored = storageService.upload(
                        upload.bytes(),
                        MANUAL_PDF_KEY_PREFIX,
                        upload.file().getOriginalFilename(),
                        PDF_CONTENT_TYPE
                );
                deleteStoredFileAfterRollback(stored.objectKey());
                storedObjects.add(stored);
            }
        } catch (RuntimeException exception) {
            storedObjects.forEach(storedObject -> deleteStoredFile(storedObject.objectKey()));
            throw exception;
        }
        return storedObjects;
    }

    private List<PdfUpload> readPdfUploads(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new CustomException(ErrorType.MANUAL_INVALID_FILE, "PDF file is required.");
        }

        List<PdfUpload> uploads = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new CustomException(ErrorType.MANUAL_INVALID_FILE, "PDF file is required.");
            }
            try {
                uploads.add(new PdfUpload(file, file.getBytes()));
            } catch (IOException e) {
                throw new CustomException(ErrorType.MANUAL_INVALID_FILE, "Failed to read PDF file.");
            }
        }
        return uploads;
    }

    private String extractContent(List<PdfUpload> uploads) {
        return uploads.stream()
                .map(upload -> pdfTextExtractor.extract(upload.file(), upload.bytes()))
                .reduce((left, right) -> left + "\n\n" + right)
                .orElse("");
    }

    private void deleteStoredFile(String fileKey) {
        if (fileKey != null && !fileKey.isBlank()) {
            try {
                storageService.deleteObject(fileKey);
            } catch (RuntimeException e) {
                log.warn("Failed to delete manual PDF file objectKey={}", fileKey, e);
            }
        }
    }

    private void deleteStoredFileAfterCommit(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteStoredFile(fileKey);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteStoredFile(fileKey);
            }
        });
    }

    private void deleteStoredFileAfterRollback(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    deleteStoredFile(fileKey);
                }
            }
        });
    }

    private void saveVersion(Manual manual, Long actorUserId, String manualNum, String updateReason) {
        if (manualVersionRepository.existsByManualManualIdAndManualNumAndDeletedAtIsNull(manual.getManualId(), manualNum)) {
            throw new CustomException(ErrorType.CONFLICT, "Manual version already exists. manualNum=" + manualNum);
        }
        manualVersionRepository.save(ManualVersion.create(manual, actorUserId, manualNum, normalizeUpdateReason(updateReason)));
    }

    private String resolveNextVersion(Manual manual, int nextFileCount) {
        String latestVersion = manualVersionRepository
                .findTopByManualManualIdAndDeletedAtIsNullOrderByManualVersionIdDesc(manual.getManualId())
                .map(ManualVersion::getManualNum)
                .orElse(manual.getVersion());

        boolean fileCountChanged = fileCount(manual) != nextFileCount;
        return incrementVersion(latestVersion, fileCountChanged);
    }

    private int fileCount(Manual manual) {
        long count = manualFileRepository.countByManualManualIdAndDeletedAtIsNull(manual.getManualId());
        if (count > 0) {
            return Math.toIntExact(count);
        }
        return manual.getFileKey() == null || manual.getFileKey().isBlank() ? 0 : 1;
    }

    private String incrementVersion(String latestVersion, boolean fileCountChanged) {
        VersionParts version = VersionParts.parse(latestVersion);
        if (fileCountChanged) {
            return (version.major() + 1) + ".0";
        }

        int nextMinor = version.minor() + 1;
        if (nextMinor >= 10) {
            return (version.major() + 1) + ".0";
        }
        return version.major() + "." + nextMinor;
    }

    private String normalizeUpdateReason(String updateReason) {
        if (updateReason == null || updateReason.isBlank()) {
            return "ADMIN_UPDATE";
        }
        return updateReason;
    }

    private List<ManualFile> findActiveFiles(Long manualId) {
        return manualFileRepository.findByManualManualIdAndDeletedAtIsNullOrderBySortOrderAsc(manualId);
    }

    private List<String> findFileUrls(Long manualId) {
        return findActiveFiles(manualId).stream()
                .map(ManualFile::getFileUrl)
                .toList();
    }

    private List<String> toFileUrls(List<StoredObject> storedObjects) {
        return storedObjects.stream()
                .map(StoredObject::publicUrl)
                .toList();
    }

    private void attachFiles(Manual manual, List<StoredObject> storedObjects) {
        for (int index = 0; index < storedObjects.size(); index++) {
            StoredObject storedObject = storedObjects.get(index);
            manualFileRepository.save(ManualFile.create(manual, storedObject.objectKey(), storedObject.publicUrl(), index + 1));
        }
        StoredObject first = storedObjects.get(0);
        manual.attachFile(first.objectKey(), first.publicUrl());
    }

    private void replaceFiles(Manual manual, List<ManualFile> previousFiles, List<StoredObject> storedObjects) {
        previousFiles.forEach(ManualFile::delete);
        attachFiles(manual, storedObjects);
    }

    private record VersionParts(int major, int minor) {
        private static VersionParts parse(String version) {
            if (version == null || version.isBlank()) {
                return new VersionParts(1, 0);
            }

            String normalized = version.trim();
            if (normalized.startsWith("v") || normalized.startsWith("V")) {
                normalized = normalized.substring(1);
            }

            String[] parts = normalized.split("\\.");
            try {
                int major = Integer.parseInt(parts[0]);
                int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
                return new VersionParts(Math.max(major, 1), Math.max(minor, 0));
            } catch (NumberFormatException exception) {
                return new VersionParts(1, 0);
            }
        }
    }

    private record PdfUpload(MultipartFile file, byte[] bytes) {
    }
}
